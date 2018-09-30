package de.skuzzle.enforcer.restrictimports.analyze;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class ImportMatcherImpl implements ImportMatcher {

    private final LineSupplier supplier;

    ImportMatcherImpl(LineSupplier supplier) {
        this.supplier = supplier;
    }

    @Override
    public Optional<MatchedFile> matchFile(Path sourceFile, BannedImportGroups groups) {
        final List<MatchedImport> matches = new ArrayList<>();
        try (final Stream<String> lines = this.supplier.lines(sourceFile)) {

            final Iterable<String> lineIt = lines.map(String::trim)::iterator;

            int row = 1;
            final String javaFileName = getJavaFileName(sourceFile);
            // select group default in case this file has no package statement
            BannedImportGroup group = groups.selectGroupFor(javaFileName).orElse(null);

            for (final Iterator<String> it = lineIt.iterator(); it.hasNext(); ++row) {
                final String line = it.next();
                if (line.isEmpty()) {
                    continue;
                } else if (isPackage(line)) {
                    // package ...; statement

                    // INVARIANT: our own package name occurs in the first non-empty line
                    // of the java source file (after trimming leading comments)
                    final String packageName = extractPackageName(line);
                    final String fqcn = guessFQCN(packageName, javaFileName);

                    final Optional<BannedImportGroup> groupMatch = groups.selectGroupFor(fqcn);
                    if (!groupMatch.isPresent()) {
                        return Optional.empty();
                    }
                    group = groupMatch.get();
                    continue;
                }

                if (!isImport(line)) {
                    // as we are skipping empty (and comment) lines, by the time we
                    // encounter a non-import line we can stop processing this file
                    if (matches.isEmpty()) {
                        return Optional.empty();
                    }
                    return Optional.of(new MatchedFile(sourceFile, matches, group));
                }

                final String importName = extractPackageName(line);
                if (group.allowedImportMatches(importName)) {
                    continue;
                }
                final int lineNumber = row;
                group.ifImportIsBanned(importName)
                        .map(bannedImport -> new MatchedImport(lineNumber, importName, bannedImport))
                        .ifPresent(matches::add);

            }

            if (matches.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new MatchedFile(sourceFile, matches, group));
        } catch (final RuntimeIOException e) {
            throw e;
        } catch (final IOException e) {
            throw new RuntimeIOException(String.format(
                    "Encountered IOException while analyzing %s for banned imports",
                    sourceFile), e);
        }
    }

    private String guessFQCN(String packageName, String javaFileName) {
        return packageName.isEmpty()
                ? javaFileName
                : packageName + "." + javaFileName;
    }

    private String getJavaFileName(Path file) {
        final String s = file.getFileName().toString();
        final int i = s.lastIndexOf(".java");
        return s.substring(0, i);
    }

    private static String extractPackageName(String line) {
        final int spaceIdx = line.indexOf(" ");
        final int semiIdx = line.indexOf(";");
        final String sub = line.substring(spaceIdx, semiIdx);
        return sub.trim();
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

}
