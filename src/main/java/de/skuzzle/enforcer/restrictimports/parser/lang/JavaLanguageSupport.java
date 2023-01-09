package de.skuzzle.enforcer.restrictimports.parser.lang;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import de.skuzzle.enforcer.restrictimports.parser.ImportStatement;
import de.skuzzle.enforcer.restrictimports.parser.ImportType;
import de.skuzzle.enforcer.restrictimports.parser.ParsedFile;
import de.skuzzle.enforcer.restrictimports.util.Whitespaces;

public class JavaLanguageSupport implements LanguageSupport {

    private static final String STATIC_PREFIX = "static";
    private static final String IMPORT_STATEMENT = "import ";
    private static final String PACKAGE_STATEMENT = "package ";

    private static final JavaCompilationUnitParser javaCUParser = new JavaCompilationUnitParser();

    @Override
    public Set<String> getSupportedFileExtensions() {
        return Collections.singleton("java");
    }

    @Override
    public boolean parseFullCompilationUnitSupported() {
        return true;
    }

    @Override
    public ParsedFile parseCompilationUnit(Path sourceFilePath, Charset charset) throws IOException {
        return javaCUParser.parseCompilationUnit(sourceFilePath, charset);
    }

    @Override
    public Optional<String> parsePackage(String line) {
        if (!isPackage(line)) {
            return Optional.empty();
        }

        return Optional.of(extractPackageName(line));
    }

    @Override
    public List<ImportStatement> parseImport(String line, int lineNumber) {
        if (!isImport(line)) {
            return Collections.emptyList();
        }

        // There can be multiple import statements within the same line, so
        // we simply split them at their ';'
        final String[] parts = line.split(";");
        return Arrays.stream(parts)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.substring(IMPORT_STATEMENT.length()))
                .map(String::trim)
                .map(importName -> toImportStatement(importName, lineNumber))
                .collect(Collectors.toList());
    }

    private ImportStatement toImportStatement(String importName, int lineNumber) {
        if (importName.startsWith(STATIC_PREFIX)) {
            final String realImportName = Whitespaces.trimAll(importName.substring(STATIC_PREFIX.length()));
            return new ImportStatement(realImportName, lineNumber, ImportType.STATIC_IMPORT);
        }
        return new ImportStatement(importName, lineNumber, ImportType.IMPORT);
    }

    private boolean is(String compare, String line) {
        return line.startsWith(compare) && line.endsWith(";");
    }

    private boolean isPackage(String line) {
        return is(PACKAGE_STATEMENT, line);
    }

    private boolean isImport(String line) {
        return is(IMPORT_STATEMENT, line);
    }

    private static String extractPackageName(String line) {
        final int spaceIdx = line.indexOf(" ");
        final int semiIdx = line.indexOf(";");
        final String sub = line.substring(spaceIdx, semiIdx);
        return sub.trim();
    }

}
