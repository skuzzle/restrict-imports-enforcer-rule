package de.skuzzle.enforcer.restrictimports.analyze;

import com.google.common.base.Preconditions;
import de.skuzzle.enforcer.restrictimports.io.RuntimeIOException;
import de.skuzzle.enforcer.restrictimports.parser.ImportStatementParser;
import de.skuzzle.enforcer.restrictimports.parser.lang.LanguageSupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

final class SourceTreeAnalyzerImpl implements SourceTreeAnalyzer {

    private final Map<String, LanguageSupport> languageSupportMap;
    private final ImportAnalyzer importAnalyzer;
    private final Predicate<Path> supportedFileTypes;

    SourceTreeAnalyzerImpl() {
        this.importAnalyzer = new ImportAnalyzer();
        this.languageSupportMap = LanguageSupport.lookupImplementations();
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
                        .map(sourceFile -> fileParser.parse(sourceFile, getLanguageSupport(sourceFile)))
                        .map(parsedFile -> importAnalyzer.matchFile(parsedFile, groups))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(matchedFiles::add);
            }
        }
        return matchedFiles;
    }

    private Stream<Path> listFiles(Path root, Predicate<Path> filter) {
        try {
            if (!Files.exists(root)) {
                return Stream.empty();
            }

            return Files.find(root, Integer.MAX_VALUE, (path, bfa) -> filter.test(path));
        } catch (final IOException e) {
            throw new RuntimeIOException("Encountered IOException while listing files of " + root, e);
        }
    }

    private LanguageSupport getLanguageSupport(Path sourceFile) {
        final String normalizedExtension = getFileExtension(sourceFile);
        final LanguageSupport languageSupport = this.languageSupportMap.get(normalizedExtension);
        Preconditions.checkArgument(languageSupport != null,
                "Could not find a LanguageSupport implementation for normalized file extension: '%s' (%s)",
                normalizedExtension, sourceFile);
        return languageSupport;
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

            final String normalizedExtension = getFileExtension(path);
            return languageSupportMap.containsKey(normalizedExtension);
        }
    }

    private String getFileExtension(Path path) {
        final String fileName = path.getFileName().toString();
        final int index = fileName.lastIndexOf(".");

        if (index == -1) {
            return "";
        }

        final String extension = fileName.substring(index);
        return LanguageSupport.determineNormalizedExtension(extension);
    }
}
