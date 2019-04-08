package de.skuzzle.enforcer.restrictimports.analyze;

import com.google.common.base.Preconditions;
import de.skuzzle.enforcer.restrictimports.parser.LineSupplier;
import de.skuzzle.enforcer.restrictimports.parser.SkipCommentsLineSupplier;
import de.skuzzle.enforcer.restrictimports.parser.SourceFileParser;
import de.skuzzle.enforcer.restrictimports.parser.SourceLineParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

final class SourceTreeAnalyzerImpl implements SourceTreeAnalyzer {

    private final Map<String, SourceLineParser> sourceFileParsers;
    private final ImportMatcher importMatcher;

    public SourceTreeAnalyzerImpl() {
        this.importMatcher = new ImportMatcher();
        final ServiceLoader<SourceLineParser> serviceProvider = ServiceLoader.load(SourceLineParser.class);
        final Map<String, SourceLineParser> parsers = new HashMap<>();
        serviceProvider.forEach(parser -> {
            parser.getSupportedFileExtensions().forEach(extension -> {
                final String normalizedExtension = extension.startsWith(".")
                        ? extension.toLowerCase()
                        : "." + extension.toLowerCase();

                if (parsers.put(normalizedExtension, parser) != null) {
                    throw new IllegalStateException(
                            "There are multiple parsers to handle file extension: " + normalizedExtension);
                }
            });
        });
        Preconditions.checkState(!parsers.isEmpty(), "No SourceFileParer instances found!");
        this.sourceFileParsers = parsers;
    }

    @Override
    public AnalyzeResult analyze(AnalyzerSettings settings, BannedImportGroups groups) {
        final SourceFileParser fileParser = SourceFileParser.defaultInstance(settings.getSourceFileCharset());
        final List<MatchedFile> matchedFiles = new ArrayList<>();

        final Iterable<Path> rootsIterable = settings.getRootDirectories();
        for (final Path root : rootsIterable) {
            listFiles(root, new SourceFileMatcher())
                    .map(sourceFile -> fileParser.analyze(sourceFile, sourceFileParsers.get(getFileExtension(sourceFile))))
                    .map(parsedFile -> importMatcher.matchFile(parsedFile, groups))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(matchedFiles::add);
        }

        return AnalyzeResult.builder()
                .withMatches(matchedFiles)
                .build();
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

    private boolean isFile(Path path) {
        return !Files.isDirectory(path);
    }

    private class SourceFileMatcher implements Predicate<Path> {

        @Override
        public boolean test(Path path) {
            if (!isFile(path)) {
                return false;
            }

            final String extension = getFileExtension(path);

            if (extension == null) {
                return false;
            }

            return sourceFileParsers.containsKey(extension.toLowerCase());
        }
    }

    private String getFileExtension(Path path) {
        final String lowerCaseFileName = path.getFileName().toString().toLowerCase();
        final int index = lowerCaseFileName.lastIndexOf(".");

        if (index == -1) {
            return null;
        }

        return lowerCaseFileName.substring(index);
    }
}
