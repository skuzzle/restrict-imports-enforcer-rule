package de.skuzzle.enforcer.restrictimports.analyze;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

final class SourceTreeAnalyzerImpl implements SourceTreeAnalyzer {

    @Override
    public AnalyzeResult analyze(AnalyzerSettings settings, BannedImportGroups groups) {
        final LineSupplier lineSupplier = new SkipCommentsLineSupplier(
                settings.getSourceFileCharset(),
                settings.getCommentLineBufferSize());

        // TODO: importMatcher should be injected rather than being created here
        final ImportMatcher importMatcher = new ImportMatcherImpl(lineSupplier);

        final List<MatchedFile> matchedFiles = new ArrayList<>();

        final Iterable<Path> rootsIterable = settings.getRootDirectories();
        for (final Path root : rootsIterable) {
            listFiles(root, this::isJavaSourceFile)
                    .map(sourceFile -> importMatcher.matchFile(sourceFile, groups))
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

    private boolean isJavaSourceFile(Path path) {
        return isFile(path) && path.getFileName().toString().toLowerCase().endsWith(".java");
    }

}
