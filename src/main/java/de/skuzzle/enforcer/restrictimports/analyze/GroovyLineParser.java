package de.skuzzle.enforcer.restrictimports.analyze;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

class GroovyLineParser implements SourceLineParser {

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
}
