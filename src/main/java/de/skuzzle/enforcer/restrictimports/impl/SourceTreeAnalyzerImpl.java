package de.skuzzle.enforcer.restrictimports.impl;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;

import de.skuzzle.enforcer.restrictimports.api.SourceTreeAnalyzer;
import de.skuzzle.enforcer.restrictimports.model.BannedImportGroup;
import de.skuzzle.enforcer.restrictimports.model.Match;
import de.skuzzle.enforcer.restrictimports.model.PackagePattern;

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

    @Override
    public void checkGroupConsistency(BannedImportGroup group)
            throws EnforcerRuleException {
        checkBannedImportsPresent(group);
        allowedImportMustMatchBasePattern(group);
    }

    private void checkBannedImportsPresent(BannedImportGroup group)
            throws EnforcerRuleException {
        if (group.getBannedImports().isEmpty()) {
            throw new EnforcerRuleException("There are no banned imports specified");
        }
    }

    private void allowedImportMustMatchBasePattern(BannedImportGroup group)
            throws EnforcerRuleException {
        for (final PackagePattern allowedImport : group.getAllowedImports()) {
            final boolean matches = group.getBasePackages().stream()
                    .anyMatch(basePackage -> basePackage.matches(allowedImport));
            if (!matches) {
                throw new EnforcerRuleException(String.format(
                        "The allowed import pattern '%s' does not match any base package.",
                        allowedImport));
            }
        }
    }
}
