package de.skuzzle.enforcer.restrictimports.parser.lang;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.skuzzle.enforcer.restrictimports.parser.ImportStatement;

public class KotlinGroovyLanguageSupport implements LanguageSupport {

    private static final String IMPORT_STATEMENT = "import ";
    private static final String PACKAGE_STATEMENT = "package ";

    @Override
    public Set<String> getSupportedFileExtensions() {
        return ImmutableSet.of("groovy", "kt");
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
        final String[] parts = line.split(";");
        return Arrays.stream(parts)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.substring(IMPORT_STATEMENT.length()))
                .map(String::trim)
                .map(this::removeAlias)
                .map(importName -> new ImportStatement(importName, lineNumber))
                .collect(Collectors.toList());
    }

    private boolean is(String compare, String line) {
        return line.startsWith(compare);
    }

    private boolean isPackage(String line) {
        return is(PACKAGE_STATEMENT, line);
    }

    private boolean isImport(String line) {
        return is(IMPORT_STATEMENT, line);
    }

    private static String extractPackageName(String line) {
        // groovy may or may not have a semicolon at the end
        final int spaceIdx = line.indexOf(" ");
        final int semiIdx = line.indexOf(";");

        if (semiIdx >= 0) {
            return line.substring(spaceIdx, semiIdx).trim();
        }

        return line.substring(spaceIdx).trim();
    }

    private String removeAlias(String packageWithAlias) {
        final int asIdx = packageWithAlias.indexOf(" as ");
        if (asIdx >= 0) {
            return packageWithAlias.substring(0, asIdx);
        }
        // no alias
        return packageWithAlias;
    }
}
