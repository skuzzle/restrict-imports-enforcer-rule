package de.skuzzle.enforcer.restrictimports.parser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import de.skuzzle.enforcer.restrictimports.io.FileExtension;
import de.skuzzle.enforcer.restrictimports.parser.lang.LanguageSupport;

/**
 * Parses a source file into a {@link ParsedFile} representation.
 */
class ImportStatementParserImpl implements ImportStatementParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportStatementParserImpl.class);

    private final LineSupplier supplier;

    /**
     * Constructor just for testing purposes.
     *
     * @param supplier The line sources
     */
    ImportStatementParserImpl(LineSupplier supplier) {
        this.supplier = supplier;
    }

    private LanguageSupport getLanguageSupport(Path sourceFilePath) {
        final String sourceFileExtension = FileExtension.fromPath(sourceFilePath);
        return LanguageSupport.getLanguageSupport(sourceFileExtension)
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "Could not find a LanguageSupport implementation for normalized file extension: '%s' (%s)",
                        sourceFileExtension, sourceFilePath)));
    }

    @Override
    public ParsedFile parse(Path sourceFilePath) {
        LOGGER.trace("Analyzing {} for imports", sourceFilePath);

        final LanguageSupport languageSupport = getLanguageSupport(sourceFilePath);
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

            return new ParsedFile(sourceFilePath, packageName, fqcn, imports);
        } catch (final IOException e) {
            throw new UncheckedIOException(String.format(
                    "Encountered IOException while analyzing %s for banned imports",
                    sourceFilePath), e);
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
