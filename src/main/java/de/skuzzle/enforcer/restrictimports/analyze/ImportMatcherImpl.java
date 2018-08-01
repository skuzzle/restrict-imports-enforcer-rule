package de.skuzzle.enforcer.restrictimports.analyze;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
    public Stream<Match> matchFile(Path file, BannedImportGroup group) {
        // Sweet abuse of Stream processing ;)
        final LineCounter counter = new LineCounter();
        final PackageExtractor packageExtractor = new PackageExtractor();
        try (Stream<String> lines = this.supplier.lines(file)) {
            return lines
                    .map(ImportMatcherImpl::trimComments)
                    .peek(counter)
                    .peek(packageExtractor)
                    .filter(ImportMatcherImpl::isImport)
                    // package statement must always occur before the first import
                    // statement, thus package is known by the time the prev. filter
                    // has matched
                    .peek(includeClass(file, packageExtractor, group))
                    .map(ImportMatcherImpl::extractPackageName)
                    .filter(matchesAnyPattern(group.getBannedImports()))
                    .filter(matchesAnyPattern(group.getAllowedImports()).negate())
                    .map(toMatch(counter::getLine, file))
                    // need to copy because underlying stream is closed by try-resources
                    .collect(Collectors.toList()).stream();
        } catch (final IOException e) {
            throw new RuntimeIOException(e);
        } catch (final PrematureAbortion ignore) {
            // the processed file's package did not match the group's basePackage or
            // matched any exclusion pattern
            return Stream.empty();
        }
    }

    private static Consumer<String> includeClass(Path file, PackageExtractor extractor,
            BannedImportGroup group) {
        return line -> {
            final String javaFileName = getJavaFileName(file);
            final String javaResource = extractor.getPackageName() + "." + javaFileName;
            final boolean matchBasePattern = group.getBasePackages().stream()
                    .anyMatch(pattern -> pattern.matches(javaResource));

            if (!matchBasePattern) {
                throw new PrematureAbortion();
            }
            final boolean isExcluded = group.getExcludedClasses().stream()
                    .anyMatch(pattern -> pattern.matches(javaResource));
            if (isExcluded) {
                throw new PrematureAbortion();
            }

        };
    }

    private static String getJavaFileName(Path file) {
        final String s = file.getFileName().toString();
        final int i = s.lastIndexOf(".java");
        return s.substring(0, i);
    }

    private static Predicate<String> matchesAnyPattern(
            Collection<PackagePattern> patterns) {
        return packageName -> patterns.stream()
                .anyMatch(pattern -> pattern.matches(packageName));
    }

    private static Function<String, Match> toMatch(Supplier<Integer> lineGetter,
            Path filePath) {
        return matchedImport -> new Match(filePath, lineGetter.get(), matchedImport);
    }

    private static String extractPackageName(String line) {
        final int spaceIdx = line.indexOf(" ");
        final int semiIdx = line.indexOf(";");
        final String sub = line.substring(spaceIdx, semiIdx);
        return sub.trim();
    }

    private static boolean is(String compare, String line) {
        return line.startsWith(compare) && line.endsWith(";");
    }

    private static boolean isPackage(String line) {
        return is("package ", line);
    }

    private static boolean isImport(String line) {
        return is("import ", line);
    }

    private static String trimComments(String line) {
        String stripped = COMMENT_BLOCK_PATTERN.matcher(line.trim()).replaceAll("");

        final int inlineCommentIndex = stripped.indexOf("//");
        if (inlineCommentIndex >= 0) {
            stripped = stripped.substring(0, inlineCommentIndex);
        }
        return stripped;
    }

    private static class LineCounter implements Consumer<String> {

        private int line = 0;

        @Override
        public void accept(String t) {
            ++this.line;
        }

        public int getLine() {
            return this.line;
        }
    }

    private static class PackageExtractor implements Consumer<String> {
        private String packageName = "";

        @Override
        public void accept(String line) {
            if (isPackage(line)) {
                this.packageName = extractPackageName(line);
            }
        }

        public String getPackageName() {
            return this.packageName;
        }
    }

    private static class PrematureAbortion extends RuntimeException {

        /** */
        private static final long serialVersionUID = 1L;

    }
}
