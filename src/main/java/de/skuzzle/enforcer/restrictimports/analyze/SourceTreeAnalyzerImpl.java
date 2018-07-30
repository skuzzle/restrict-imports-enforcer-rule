package de.skuzzle.enforcer.restrictimports.analyze;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;

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
        allowedImportMustMatchBannedPattern(group);
        exclusionsMustMatchBasePattern(group);
    }

    private void checkBannedImportsPresent(BannedImportGroup group)
            throws EnforcerRuleException {
        if (group.getBannedImports().isEmpty()) {
            throw new EnforcerRuleException("There are no banned imports specified");
        }
    }

    private void allowedImportMustMatchBannedPattern(BannedImportGroup group)
            throws EnforcerRuleException {
        for (final PackagePattern allowedImport : group.getAllowedImports()) {
            final boolean matches = group.getBannedImports().stream()
                    .anyMatch(bannedPackage -> bannedPackage.matches(allowedImport));
            if (!matches) {
                throw new EnforcerRuleException(String.format(
                        "The allowed import pattern '%s' does not match any banned package.",
                        allowedImport));
            }
        }
    }

    private void exclusionsMustMatchBasePattern(BannedImportGroup group)
            throws EnforcerRuleException {
        for (final PackagePattern excludedClass : group.getExcludedClasses()) {
            final boolean matches = group.getBasePackages().stream()
                    .anyMatch(basePackage -> basePackage.matches(excludedClass));
            if (!matches) {
                throw new EnforcerRuleException(String.format(
                        "The exclusion pattern '%s' does not match any base package.",
                        excludedClass));
            }
        }
    }
}