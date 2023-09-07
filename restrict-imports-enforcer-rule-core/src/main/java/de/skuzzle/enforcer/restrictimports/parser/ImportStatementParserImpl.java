package de.skuzzle.enforcer.restrictimports.parser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.skuzzle.enforcer.restrictimports.parser.lang.LanguageSupport;
import de.skuzzle.enforcer.restrictimports.util.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses a source file into a {@link ParsedFile} representation.
 */
final class ImportStatementParserImpl implements ImportStatementParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportStatementParserImpl.class);

    private final LineSupplier supplier;
    private final Charset charset;
    private final boolean parseFullCompilationUnit;

    ImportStatementParserImpl(Charset charset, boolean parseFullCompilationUnit) {
        this.supplier = new SkipCommentsLineSupplier(charset);
        this.charset = charset;
        this.parseFullCompilationUnit = parseFullCompilationUnit;
    }

    @Override
    public ParsedFile parse(Path sourceFilePath) {
        LOGGER.trace("Analyzing {} for imports", sourceFilePath);

        final LanguageSupport languageSupport = LanguageSupport.getLanguageSupport(sourceFilePath);
        try {
            if (parseFullCompilationUnit && languageSupport.parseFullCompilationUnitSupported()) {
                LOGGER.debug("Using 'full-compilation-unit' parsing for {}", sourceFilePath);

                return parseCompilationUnit(sourceFilePath, languageSupport);
            } else {
                LOGGER.debug("Using 'line-based' parsing for {}", sourceFilePath);

                return parseLineByLine(sourceFilePath, languageSupport);
            }
        } catch (final IOException e) {
            LOGGER.debug("Encountered IOException while analyzing {} for banned imports",
                    sourceFilePath, e);

            return ParsedFile.failedToParse(sourceFilePath,
                    Annotation.withMessage("Encountered IOException while parsing"));
        }
    }

    private ParsedFile parseCompilationUnit(Path sourceFilePath, LanguageSupport languageSupport) throws IOException {
        try {
            return languageSupport.parseCompilationUnit(sourceFilePath, charset);
        } catch (Exception e) {
            LOGGER.debug(
                    "Full compilation unit parsing of {} resulted in failure. Falling back to line-by-line parsing",
                    sourceFilePath, e);
            return parseLineByLine(sourceFilePath, languageSupport)
                    .andAddAnnotation(Annotation.withMessage(
                            "Failed to parse in full-compilation-unit mode. Analysis might be inaccurate"));
        }
    }

    private ParsedFile parseLineByLine(Path sourceFilePath, LanguageSupport languageSupport) throws IOException {
        final List<ImportStatement> imports = new ArrayList<>();

        final String fileName = getFileNameWithoutExtension(sourceFilePath);
        try (final Stream<String> lines = this.supplier.lines(sourceFilePath)) {
            int row = 1;
            String packageName = "";
            String fqcn = fileName;
            for (final Iterator<String> it = lines.map(String::trim).iterator(); it.hasNext(); ++row) {
                final String line = it.next();

                // Implementation note: We check for empty lines here (instead of in
                // LineSupplier implementation)
                // so that we are able to keep track of correct line numbers.
                if (line.isEmpty()) {
                    continue;
                }

                final Optional<String> packageDeclaration = languageSupport.parsePackage(line);
                if (packageDeclaration.isPresent()) {
                    Preconditions.checkState(packageName.isEmpty(), "found duplicate package statement in '%s'",
                            sourceFilePath);
                    // package ...; statement

                    // INVARIANT: our own package name occurs in the first non-empty line
                    // of the java source file (after trimming leading comments)
                    packageName = packageDeclaration.get();
                    fqcn = guessFQCN(packageName, fileName);
                    LOGGER.trace("Guessed full qualified class name from {} and {}: '{}'", packageName, fileName, fqcn);
                    continue;
                }

                final List<ImportStatement> importStatements = languageSupport.parseImport(line, row);
                if (importStatements.isEmpty()) {
                    // as we are skipping empty (and comment) lines, by the time we
                    // encounter a non-import line we can stop processing this file
                    break;
                }
                imports.addAll(importStatements);
            }

            return ParsedFile.successful(sourceFilePath, packageName, fqcn, imports);
        }
    }

    private String guessFQCN(String packageName, String sourceFileName) {
        return packageName.isEmpty()
                ? sourceFileName
                : packageName + "." + sourceFileName;
    }

    private String getFileNameWithoutExtension(Path file) {
        final String s = file.getFileName().toString();
        final int i = s.lastIndexOf(".");
        return s.substring(0, i);
    }
}
