package de.skuzzle.enforcer.restrictimports.impl;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.skuzzle.enforcer.restrictimports.BannedImportGroup;
import de.skuzzle.enforcer.restrictimports.IOUtils;
import de.skuzzle.enforcer.restrictimports.ImportMatcher;
import de.skuzzle.enforcer.restrictimports.Match;
import de.skuzzle.enforcer.restrictimports.SourceTreeAnalyzer;

final class SourceTreeAnalyzerImpl implements SourceTreeAnalyzer {

    private final ImportMatcher matcher;
    private final IOUtils ioUtil;

    public SourceTreeAnalyzerImpl(ImportMatcher matcher, IOUtils ioUtils) {
        this.matcher = matcher;
        this.ioUtil = ioUtils;
    }

    @Override
    public Map<String, List<Match>> analyze(Stream<Path> roots, BannedImportGroup group) {
        return roots.flatMap(root -> this.ioUtil.listFiles(root, this::isJavaSourceFile))
                .flatMap(path -> this.matcher.matchFile(path, group))
                .collect(Collectors.groupingBy(Match::getSourceFile));
    }

    private boolean isJavaSourceFile(Path path) {
        return this.ioUtil.isFile(path) &&
                path.getFileName().toString().toLowerCase().endsWith(".java");
    }

}
