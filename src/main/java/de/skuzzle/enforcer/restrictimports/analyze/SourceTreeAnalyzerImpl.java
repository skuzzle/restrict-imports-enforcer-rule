package de.skuzzle.enforcer.restrictimports.analyze;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.skuzzle.enforcer.restrictimports.parser.ImportStatementParser;
import de.skuzzle.enforcer.restrictimports.parser.ParsedFile;
import de.skuzzle.enforcer.restrictimports.parser.lang.LanguageSupport;

final class SourceTreeAnalyzerImpl implements SourceTreeAnalyzer {

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

        final Collection<MatchedFile> srcMatches = analyzeDirectories(groups, fileParser, settings.getSrcDirectories());
        final Collection<MatchedFile> testMatches = analyzeDirectories(groups, fileParser,
                settings.getTestDirectories());

        final long stop = System.currentTimeMillis();
        final long duration = stop - start;
        return AnalyzeResult.builder()
                .withMatches(srcMatches)
                .withMatchesInTestCode(testMatches)
                .withDuration(duration)
                .build();
    }

    private Collection<MatchedFile> analyzeDirectories(BannedImportGroups groups, ImportStatementParser fileParser,
            Iterable<Path> directories) {
        final Collection<MatchedFile> matchedFiles = new ArrayList<>();
        for (final Path srcDir : directories) {
            try (Stream<Path> sourceFiles = listFiles(srcDir, supportedFileTypes)) {
                sourceFiles
                        .map(fileParser::parse)
                        .map(analyzeAgainst(groups))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(matchedFiles::add);
            }
        }
        return matchedFiles;
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

}
