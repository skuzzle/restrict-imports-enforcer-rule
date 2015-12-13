package de.skuzzle.enforcer.restrictimports.impl;

import java.nio.file.Path;
import java.util.Collection;
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
    public Map<String, List<Match>> analyze(Stream<Path> roots,
            Collection<BannedImportGroup> groups) {
        return roots.flatMap(root -> this.ioUtil.listFiles(root, this::isJavaSourceFile))
                .flatMap(root -> matchAll(root, groups))
                .collect(Collectors.groupingBy(Match::getSourceFile));
    }

    private Stream<Match> matchAll(Path file, Collection<BannedImportGroup> groups) {
        return groups.stream()
                .flatMap(group -> this.matcher.matchFile(file, group));
    }

    private boolean isJavaSourceFile(Path path) {
        return this.ioUtil.isFile(path) &&
                path.getFileName().toString().toLowerCase().endsWith(".java");
    }

}
