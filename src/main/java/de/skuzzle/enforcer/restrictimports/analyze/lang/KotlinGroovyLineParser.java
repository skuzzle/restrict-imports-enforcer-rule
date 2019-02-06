package de.skuzzle.enforcer.restrictimports.analyze.lang;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.skuzzle.enforcer.restrictimports.analyze.SourceLineParser;

public class KotlinGroovyLineParser implements SourceLineParser {

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
    public List<String> parseImport(String line) {
        if (!isImport(line)) {
            return ImmutableList.of();
        }
        final String packageWithAlias = extractPackageName(line);
        return ImmutableList.of(removeAlias(packageWithAlias));
    }

    private boolean is(String compare, String line) {
        return line.startsWith(compare);
    }

    private boolean isPackage(String line) {
        return is("package ", line);
    }

    private boolean isImport(String line) {
        return is("import ", line);
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
