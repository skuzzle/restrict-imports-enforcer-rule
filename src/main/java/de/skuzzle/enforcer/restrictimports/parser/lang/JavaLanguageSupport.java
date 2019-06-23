package de.skuzzle.enforcer.restrictimports.parser.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.skuzzle.enforcer.restrictimports.parser.ImportStatement;

public class JavaLanguageSupport implements LanguageSupport {

    private static final String IMPORT_STATEMENT = "import ";
    private static final String PACKAGE_STATEMENT = "package ";

    @Override
    public Set<String> getSupportedFileExtensions() {
        return ImmutableSet.of("java");
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
            return ImmutableList.of();
        }

        // There can be multiple import statements within the same line, so
        // we simply split them at their ';'
        final String trimmed = line.trim();
        int start = 0;
        int semiIdx = trimmed.indexOf(';');
        final List<ImportStatement> imports = new ArrayList<>();
        while (semiIdx > 0) {
            final String importStatement = trimmed.substring(start, semiIdx);
            final String importName = importStatement
                    .trim()
                    .substring(IMPORT_STATEMENT.length())
                    .trim();

            imports.add(new ImportStatement(importName, lineNumber));

            start = semiIdx + 1;
            semiIdx = trimmed.indexOf(';', start);
        }

        return imports;
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
