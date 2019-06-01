package de.skuzzle.enforcer.restrictimports.analyze;

import de.skuzzle.enforcer.restrictimports.parser.ImportStatementParser;
import de.skuzzle.enforcer.restrictimports.parser.ParsedFile;
import de.skuzzle.enforcer.restrictimports.parser.lang.LanguageSupport;

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

final class SourceTreeAnalyzerImpl implements SourceTreeAnalyzer {

    private final ImportAnalyzer importAnalyzer;
    private final Predicate<Path> supportedFileTypes;

    SourceTreeAnalyzerImpl() {
        this.importAnalyzer = new ImportAnalyzer();
        this.supportedFileTypes = new SourceFileMatcher();
    }

    @Override
    public AnalyzeResult analyze(AnalyzerSettings settings, BannedImportGroups groups) {
        final ImportStatementParser fileParser = ImportStatementParser.defaultInstance(settings.getSourceFileCharset());

        final Collection<MatchedFile> srcMatches = analyzeDirectories(groups, fileParser, settings.getSrcDirectories());
        final Collection<MatchedFile> testMatches = analyzeDirectories(groups, fileParser, settings.getTestDirectories());

        return AnalyzeResult.builder()
                .withMatches(srcMatches)
                .withMatchesInTestCode(testMatches)
                .build();
    }

    private Collection<MatchedFile> analyzeDirectories(BannedImportGroups groups, ImportStatementParser fileParser, Iterable<Path> directories) {
        final Collection<MatchedFile> matchedFiles = new ArrayList<>();
        for (final Path srcDir : directories) {
            try (Stream<Path> sourceFiles = listFiles(srcDir, supportedFileTypes)) {
                sourceFiles
                        .map(parseFileUsing(fileParser))
                        .map(analyzeAgainst(groups))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(matchedFiles::add);
            }
        }
        return matchedFiles;
    }

    private Function<Path, ParsedFile> parseFileUsing(ImportStatementParser parser) {
        return sourceFile -> parser.parse(sourceFile, getLanguageSupport(sourceFile));
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

    private LanguageSupport getLanguageSupport(Path sourceFile) {
        final String extension = getFileExtension(sourceFile);
        return LanguageSupport.getLanguageSupport(extension)
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "Could not find a LanguageSupport implementation for normalized file extension: '%s' (%s)",
                        extension, sourceFile)));
    }

    private boolean isFile(Path path) {
        return !Files.isDirectory(path);
    }

    /**
     * Predicate that matches source files for which a {@link LanguageSupport} implementation is known.
     */
    private class SourceFileMatcher implements Predicate<Path> {

        @Override
        public boolean test(Path path) {
            if (!isFile(path)) {
                return false;
            }

            final String extension = getFileExtension(path);
            return LanguageSupport.isLanguageSupported(extension);
        }
    }

    private String getFileExtension(Path path) {
        final String fileName = path.getFileName().toString();
        final int index = fileName.lastIndexOf(".");

        if (index == -1) {
            return "";
        }

        return fileName.substring(index);
    }
}
