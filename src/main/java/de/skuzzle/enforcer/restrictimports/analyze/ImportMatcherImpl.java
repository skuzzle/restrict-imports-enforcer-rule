package de.skuzzle.enforcer.restrictimports.analyze;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class ImportMatcherImpl implements ImportMatcher {

    private static final Pattern COMMENT_BLOCK_PATTERN = Pattern.compile("/\\*.*?\\*/");

    public interface LineSupplier {
        Stream<String> lines(Path path) throws IOException;
    }

    private final LineSupplier supplier;

    ImportMatcherImpl(LineSupplier supplier) {
        this.supplier = supplier;
    }

    @Override
    public Stream<MatchedImport> matchFile(Path sourceFile, BannedImportGroup group) {

        final List<MatchedImport> matches = new ArrayList<>();
        try (final Stream<String> lines = this.supplier.lines(sourceFile)) {

            final Iterable<String> lineIt = lines
                    .map(this::trimComments)::iterator;

            int row = 1;
            String packageName = "";
            boolean blockCommentStarted = false;
            for (final Iterator<String> it = lineIt.iterator(); it.hasNext(); ++row) {
                final String line = it.next();
                if (line.isEmpty()) {
                    continue;
                } else if (line.startsWith("/*")) {
                    blockCommentStarted = true;
                    continue;
                } else if (blockCommentStarted) {
                    if (line.indexOf("*/") >= 0) {
                        blockCommentStarted = false;
                    }
                    continue;
                } else if (isPackage(line)) {
                    // INVARIANT: our own package name occurs in the first non-empty line
                    // of the java source file (after trimming leading comments)
                    packageName = extractPackageName(line);
                    continue;
                }

                if (isExcluded(sourceFile, packageName, group)) {
                    return Stream.empty();
                } else if (!isImport(line)) {
                    // as we are skipping empty (and comment) lines,
                    continue;
                }

                final String importName = extractPackageName(line);
                if (matchesAnyPattern(importName, group.getAllowedImports())) {
                    continue;
                }
                final int lineNumber = row;
                group.getBannedImports().stream()
                        .filter(bannedImport -> bannedImport.matches(importName))
                        .findFirst()
                        .map(matchedBy -> new MatchedImport(lineNumber, importName,
                                matchedBy))
                        .ifPresent(matches::add);

            }

            return matches.stream();
        } catch (final RuntimeIOException e) {
            throw e;
        } catch (final IOException e) {
            throw new RuntimeIOException(String.format(
                    "Encountered IOException while analyzing %s for banned imports",
                    sourceFile), e);
        }
    }

    private boolean isExcluded(Path sourceFile, String packageName,
            BannedImportGroup group) {
        final String javaFileName = getJavaFileName(sourceFile);
        final String fqcn = guessFQCN(packageName, javaFileName);
        final boolean matchBasePattern = group.getBasePackages().stream()
                .anyMatch(pattern -> pattern.matches(fqcn));

        if (!matchBasePattern) {
            return true;
        }
        final boolean matchExclusion = group.getExcludedClasses().stream()
                .anyMatch(pattern -> pattern.matches(fqcn));
        return matchExclusion;
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

    private boolean matchesAnyPattern(String packageName,
            Collection<PackagePattern> patterns) {
        return patterns.stream()
                .anyMatch(pattern -> pattern.matches(packageName));
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

    private String trimComments(String line) {
        String stripped = COMMENT_BLOCK_PATTERN.matcher(line.trim()).replaceAll("");

        final int inlineCommentIndex = stripped.indexOf("//");
        if (inlineCommentIndex >= 0) {
            stripped = stripped.substring(0, inlineCommentIndex);
        }
        return stripped;
    }

}
