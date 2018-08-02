package de.skuzzle.enforcer.restrictimports.analyze;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class SourceTreeAnalyzerImpl implements SourceTreeAnalyzer {

    private final ImportMatcher matcher;
    private final IOUtils ioUtil;

    public SourceTreeAnalyzerImpl(ImportMatcher matcher, IOUtils ioUtils) {
        this.matcher = matcher;
        this.ioUtil = ioUtils;
    }

    @Override
    public AnalyzeResult analyze(Stream<Path> roots, BannedImportGroup group) {
        final Iterable<Path> rootsIterable = roots::iterator;
        final List<MatchedFile> matchedFiles = new ArrayList<>();

        for (final Path root : rootsIterable) {
            final Iterable<Path> sourceFilesIterable = ioUtil.listFiles(root,
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

    private boolean isJavaSourceFile(Path path) {
        return this.ioUtil.isFile(path) &&
                path.getFileName().toString().toLowerCase().endsWith(".java");
    }

}
