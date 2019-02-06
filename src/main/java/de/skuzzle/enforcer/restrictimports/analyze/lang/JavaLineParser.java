package de.skuzzle.enforcer.restrictimports.analyze.lang;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.skuzzle.enforcer.restrictimports.analyze.SourceLineParser;

public class JavaLineParser implements SourceLineParser {

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
    public List<String> parseImport(String line) {
        if (!isImport(line)) {
            return ImmutableList.of();
        }

        return ImmutableList.of(extractPackageName(line));
    }

    private boolean is(String compare, String line) {
        return line.startsWith(compare) && line.endsWith(";");
    }

    private boolean isPackage(String line) {
        return is("package ", line);
    }

    private boolean isImport(String line) {
        return is("import ", line);
    }

    private static String extractPackageName(String line) {
        final int spaceIdx = line.indexOf(" ");
        final int semiIdx = line.indexOf(";");
        final String sub = line.substring(spaceIdx, semiIdx);
        return sub.trim();
    }

}
