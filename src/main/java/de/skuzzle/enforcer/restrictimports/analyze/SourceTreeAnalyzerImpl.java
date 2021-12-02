package de.skuzzle.enforcer.restrictimports.analyze;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.skuzzle.enforcer.restrictimports.parser.ImportStatementParser;
import de.skuzzle.enforcer.restrictimports.parser.ParsedFile;
import de.skuzzle.enforcer.restrictimports.parser.lang.LanguageSupport;

final class SourceTreeAnalyzerImpl implements SourceTreeAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceTreeAnalyzerImpl.class);

    private final ImportAnalyzer importAnalyzer;
    private final Predicate<Path> supportedFileTypes;

    SourceTreeAnalyzerImpl() {
        this.importAnalyzer = new ImportAnalyzer();
        this.supportedFileTypes = LanguageSupport::isLanguageSupported;
    }

    @Override
    public AnalyzeResult analyze(AnalyzerSettings settings, BannedImportGroups groups) {
        final long start = System.currentTimeMillis();
        final ImportStatementParser fileParser = ImportStatementParser.forCharset(settings.getSourceFileCharset());

        if (settings.isParallel()) {
            LOGGER.warn("EXPERIMENTAL FEATURE enabled. You have enabled parallel analysis which is marked as "
                    + "experimental. Please be aware that experimental features might get removed. "
                    + "Please share your feedback!");
        }

        final ThreadSafeCounter counter = new ThreadSafeCounter();
        final Collection<MatchedFile> srcMatches = analyzeDirectories(groups, fileParser,
                settings.getSrcDirectories(), settings.isParallel(), counter);
        final Collection<MatchedFile> testMatches = analyzeDirectories(groups, fileParser,
                settings.getTestDirectories(), settings.isParallel(), counter);

        final long stop = System.currentTimeMillis();
        final long duration = stop - start;
        return AnalyzeResult.builder()
                .withMatches(srcMatches)
                .withMatchesInTestCode(testMatches)
                .withDuration(duration)
                .withAnalysedFileCount(counter.count())
                .build();
    }

    private Collection<MatchedFile> analyzeDirectories(BannedImportGroups groups, ImportStatementParser fileParser,
            Iterable<Path> directories, boolean parallel, ThreadSafeCounter counter) {
        return StreamSupport.stream(directories.spliterator(), parallel)
                .flatMap(srcDir -> analyzeDirectory(groups, fileParser, srcDir, parallel, counter))
                .collect(Collectors.toList());
    }

    private Stream<MatchedFile> analyzeDirectory(BannedImportGroups groups, ImportStatementParser fileParser,
            Path srcDir, boolean parallel, ThreadSafeCounter counter) {

        try (Stream<Path> sourceFiles = parallelize(listFiles(srcDir, supportedFileTypes), parallel)) {
            final List<MatchedFile> matches = sourceFiles
                    .peek(counter)
                    .map(fileParser::parse)
                    .map(analyzeAgainst(groups))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            return parallelize(matches.stream(), parallel);
        }
    }

    private <T> Stream<T> parallelize(Stream<T> stream, boolean parallel) {
        return parallel
                ? stream.parallel()
                : stream;
    }

    private Function<ParsedFile, Optional<MatchedFile>> analyzeAgainst(BannedImportGroups groups) {
        return parsedFile -> importAnalyzer.matchFile(parsedFile, groups);
    }

    private Stream<Path> listFiles(Path root, Predicate<Path> filter) {
        try {
            if (!Files.exists(root)) {
                return Stream.empty();
            }

            return Files.find(root, Integer.MAX_VALUE, (path, bfa) -> filter.test(path));
        } catch (final IOException e) {
            throw new UncheckedIOException("Encountered IOException while listing files of " + root, e);
        }
    }

    private static class ThreadSafeCounter implements Consumer<Object> {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public void accept(Object t) {
            counter.incrementAndGet();
        }

        public int count() {
            return counter.get();
        }

    }

}
