package de.skuzzle.enforcer.restrictimports;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;

class ImportMatcherImpl implements ImportMatcher {

    public interface LineSupplier {
        Stream<String> lines(Path path) throws IOException;
    }

    private final LineSupplier supplier;

    ImportMatcherImpl() {
        this(Files::lines);
    }

    @VisibleForTesting
    ImportMatcherImpl(LineSupplier supplier) {
        this.supplier = supplier;
    }

    @Override
    public Stream<Match> matchFile(Path file, Collection<PackagePattern> bannedImports,
            Collection<PackagePattern> allowed) {

        final LineCounter counter = new LineCounter();
        try (Stream<String> lines = this.supplier.lines(file)) {
            return lines.map(String::trim)
                    .peek(counter)
                    .filter(this::isImport)
                    .map(this::extractPackage)
                    .filter(matchesAnyPattern(bannedImports))
                    .filter(not(matchesAnyPattern(allowed)))
                    .map(toMatch(counter::getLine, file.toString()))
                    // need to copy because underlying stream is closed by try-resources
                    .collect(Collectors.toList()).stream();
        } catch (final IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    private static <T> Predicate<T> not(Predicate<T> predicate) {
        return t -> !predicate.test(t);
    }

    private static Predicate<String> matchesAnyPattern(
            Collection<PackagePattern> patterns) {
        return packageName -> patterns.stream()
                .anyMatch(pattern -> pattern.matches(packageName));
    }

    private Function<String, Match> toMatch(Supplier<Integer> lineGetter,
            String filePath) {
        return matchedImport ->
            new Match(filePath, lineGetter.get(), matchedImport);
    }

    private String extractPackage(String line) {
        final int spaceIdx = line.indexOf(" ");
        final int semiIdx = line.indexOf(";");
        final String sub = line.substring(spaceIdx, semiIdx);
        return sub.trim();
    }

    private boolean isImport(String line) {
        return line.startsWith("import") && line.endsWith(";");
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
}
