package de.skuzzle.enforcer.restrictimports.parser.lang;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import de.skuzzle.enforcer.restrictimports.parser.ImportStatement;

public class JavaLanguageSupport implements LanguageSupport {

    private static final String IMPORT_STATEMENT = "import ";
    private static final String PACKAGE_STATEMENT = "package ";

    @Override
    public Set<String> getSupportedFileExtensions() {
        return Collections.singleton("java");
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
                .map(importName -> new ImportStatement(importName, lineNumber))
                .collect(Collectors.toList());
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
