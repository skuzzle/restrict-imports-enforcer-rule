package de.skuzzle.enforcer.restrictimports.analyze;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class SourceTreeAnalyzerImpl implements SourceTreeAnalyzer {

    private final ImportMatcher matcher;
    private final AnalyzerSettings settings;

    public SourceTreeAnalyzerImpl(AnalyzerSettings settings, ImportMatcher matcher) {
        this.settings = settings;
        this.matcher = matcher;
    }

    @Override
    public AnalyzeResult analyze(BannedImportGroup group) {
        final Iterable<Path> rootsIterable = settings.getRootDirectories();
        final List<MatchedFile> matchedFiles = new ArrayList<>();

        for (final Path root : rootsIterable) {
            final Iterable<Path> sourceFilesIterable = listFiles(root,
                    this::isJavaSourceFile)::iterator;

            for (final Path sourceFile : sourceFilesIterable) {
                final List<MatchedImport> matches = matcher.matchFile(sourceFile, group)
                        .collect(Collectors.toList());

                if (!matches.isEmpty()) {
                    final MatchedFile matchedFile = new MatchedFile(sourceFile, matches);
                    matchedFiles.add(matchedFile);
                }
            }
        }

        return new AnalyzeResult(matchedFiles);
    }

    private Stream<Path> listFiles(Path root, Predicate<Path> filter) {
        try {
            if (!Files.exists(root)) {
                return Stream.empty();
            }
            return Files.find(root, Integer.MAX_VALUE, (path, bfa) -> filter.test(path));
        } catch (final IOException e) {
            throw new RuntimeIOException(
                    "Encountered IOException while listing files of " + root, e);
        }
    }

    private boolean isFile(Path path) {
        return !Files.isDirectory(path);
    }

    private boolean isJavaSourceFile(Path path) {
        return isFile(path) &&
                path.getFileName().toString().toLowerCase().endsWith(".java");
    }

}
