package de.skuzzle.enforcer.restrictimports;

import static com.google.common.base.Preconditions.checkArgument;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import de.skuzzle.enforcer.restrictimports.analyze.BannedImportGroup;
import de.skuzzle.enforcer.restrictimports.analyze.Match;
import de.skuzzle.enforcer.restrictimports.analyze.MatchFormatter;
import de.skuzzle.enforcer.restrictimports.analyze.PackagePattern;
import de.skuzzle.enforcer.restrictimports.analyze.SourceTreeAnalyzer;

/**
 * Enforcer rule which restricts the usage of certain packages or classes within a Java
 * code base.
 */
public class RestrictImports implements EnforcerRule {

    private static final PackagePattern DEFAULT_BASE_PACKAGE = PackagePattern.parse("**");

    private PackagePattern basePackage = DEFAULT_BASE_PACKAGE;
    private List<PackagePattern> basePackages = new ArrayList<>();

    private PackagePattern bannedImport = null;
    private List<PackagePattern> bannedImports = new ArrayList<>();

    private PackagePattern allowedImport = null;
    private List<PackagePattern> allowedImports = new ArrayList<>();

    private PackagePattern exclusion = null;
    private List<PackagePattern> exclusions = new ArrayList<>();

    private boolean includeTestCode;
    private String reason;

    private static final SourceTreeAnalyzer ANALYZER = SourceTreeAnalyzer.getInstance();

    @Override
    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
        final Log log = helper.getLog();
        try {
            final MavenProject project = (MavenProject) helper.evaluate("${project}");

            log.debug("Checking for banned imports");
            final BannedImportGroup group = createGroupFromPluginConfiguration();
            final Collection<Path> sourceRoots = listSourceRoots(project, log)
                    .collect(Collectors.toList());

            final Map<Path, List<Match>> matches = ANALYZER.analyze(
                    sourceRoots.stream(), group);

            if (!matches.isEmpty()) {
                throw new EnforcerRuleException(
                        formatErrorString(sourceRoots, group, matches));
            } else {
                log.debug("No banned imports found");
            }
        } catch (final Exception e) {
            throw new EnforcerRuleException("Encountered unexpected exception: " +
                    e.getLocalizedMessage(), e);
        }
    }

    private BannedImportGroup createGroupFromPluginConfiguration()
            throws EnforcerRuleException {
        return BannedImportGroup.builder()
                .withBasePackages(assembleList(this.basePackage, this.basePackages))
                .withBannedImports(assembleList(this.bannedImport, this.bannedImports))
                .withAllowedImports(assembleList(this.allowedImport, this.allowedImports))
                .withExcludedClasses(assembleList(this.exclusion, this.exclusions))
                .withReason(reason)
                .build();
    }

    private List<PackagePattern> assembleList(PackagePattern single,
            List<PackagePattern> multi) {
        if (single == null) {
            return multi;
        } else {
            return Collections.singletonList(single);
        }
    }

    private String formatErrorString(Collection<Path> roots, BannedImportGroup group,
            Map<Path, List<Match>> groups) {
        return MatchFormatter.getInstance().formatMatches(roots, groups, group);
    }

    @SuppressWarnings("unchecked")
    private Stream<Path> listSourceRoots(MavenProject project, Log log) {
        final Stream<String> compileStream = project.getCompileSourceRoots().stream();

        final Stream<String> rootFolders;
        if (this.includeTestCode) {
            final Stream<String> testStream = project.getTestCompileSourceRoots()
                    .stream();
            rootFolders = Stream.concat(compileStream, testStream);
        } else {
            rootFolders = compileStream;
        }

        return rootFolders
                .peek(root -> log.debug("Including source dir: " + root))
                .map(Paths::get);
    }

    @Override
    public String getCacheId() {
        return "";
    }

    @Override
    public boolean isCacheable() {
        return false;
    }

    @Override
    public boolean isResultValid(EnforcerRule rule) {
        return false;
    }

    public final void setBasePackage(String basePackage) {
        checkArgument(this.basePackages.isEmpty(),
                "Configuration error: you should either specify a single base package using <basePackage> or multiple "
                        + "base packages using <basePackages> but not both");
        this.basePackage = PackagePattern.parse(basePackage);
    }

    public final void setBasePackages(List<String> basePackages) {
        checkArgument(this.basePackage == DEFAULT_BASE_PACKAGE,
                "Configuration error: you should either specify a single base package using <basePackage> or multiple "
                        + "base packages using <basePackages> but not both");
        checkArgument(basePackages != null && !basePackages.isEmpty(),
                "bannedPackages must not be empty");
        this.basePackage = null;
        this.basePackages = PackagePattern.parseAll(basePackages);
    }

    public void setBannedImport(String bannedImport) {
        checkArgument(this.bannedImports.isEmpty(),
                "Configuration error: you should either specify a single banned import using <bannedImport> or multiple "
                        + "banned imports using <bannedImports> but not both");
        this.bannedImport = PackagePattern.parse(bannedImport);
    }

    public final void setBannedImports(List<String> bannedPackages) {
        checkArgument(this.bannedImport == null,
                "Configuration error: you should either specify a single banned import using <bannedImport> or multiple "
                        + "banned imports using <bannedImports> but not both");
        checkArgument(bannedPackages != null && !bannedPackages.isEmpty(),
                "bannedPackages must not be empty");
        this.bannedImport = null;
        this.bannedImports = PackagePattern.parseAll(bannedPackages);
    }

    public final void setAllowedImport(String allowedImport) {
        checkArgument(this.allowedImports.isEmpty(),
                "Configuration error: you should either specify a single allowed import using <allowedImport> or multiple "
                        + "allowed imports using <allowedImports> but not both");
        this.allowedImport = PackagePattern.parse(allowedImport);
    }

    public final void setAllowedImports(List<String> allowedImports) {
        checkArgument(this.allowedImport == null,
                "Configuration error: you should either specify a single allowed import using <allowedImport> or multiple "
                        + "allowed imports using <allowedImports> but not both");
        this.allowedImports = PackagePattern.parseAll(allowedImports);
    }

    public final void setExclusion(String exclusion) {
        checkArgument(this.exclusions.isEmpty(),
                "Configuration error: you should either specify a single exclusion using <exclusion> or multiple "
                        + "exclusions using <exclusions> but not both");
        this.exclusion = PackagePattern.parse(exclusion);
    }

    public final void setExclusions(List<String> exclusions) {
        checkArgument(this.exclusion == null,
                "Configuration error: you should either specify a single exclusion using <exclusion> or multiple "
                        + "exclusions using <exclusions> but not both");
        this.exclusions = PackagePattern.parseAll(exclusions);
    }

    public final void setIncludeTestCode(boolean includeTestCode) {
        this.includeTestCode = includeTestCode;
    }

    public final void setReason(String reason) {
        this.reason = reason;
    }
}