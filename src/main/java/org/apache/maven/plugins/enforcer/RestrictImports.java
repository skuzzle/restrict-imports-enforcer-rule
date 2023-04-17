package org.apache.maven.plugins.enforcer;

import static de.skuzzle.enforcer.restrictimports.util.Preconditions.checkArgument;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.enforcer.rule.api.EnforcerLevel;
import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRule2;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.skuzzle.enforcer.restrictimports.analyze.AnalyzeResult;
import de.skuzzle.enforcer.restrictimports.analyze.AnalyzerSettings;
import de.skuzzle.enforcer.restrictimports.analyze.BannedImportDefinitionException;
import de.skuzzle.enforcer.restrictimports.analyze.BannedImportGroup;
import de.skuzzle.enforcer.restrictimports.analyze.BannedImportGroups;
import de.skuzzle.enforcer.restrictimports.analyze.SourceTreeAnalyzer;
import de.skuzzle.enforcer.restrictimports.formatting.MatchFormatter;

/**
 * Enforcer rule which restricts the usage of certain packages or classes within a Java
 * code base.
 */
public class RestrictImports extends BannedImportGroupDefinition implements EnforcerRule, EnforcerRule2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestrictImports.class);

    private static final String SKIP_PROPERTY_NAME = "restrictimports.skip";
    private static final String FAIL_BUILD_PROPERTY_NAME = "restrictimports.failBuild";
    private static final String PARALLEL_ANALYSIS_PROPERTY_NAME = "restrictimports.parallel";

    private final SourceTreeAnalyzer analyzer = SourceTreeAnalyzer.getInstance();
    private final MatchFormatter matchFormatter = MatchFormatter.getInstance();

    private List<BannedImportGroupDefinition> groups = new ArrayList<>();

    private boolean includeCompileCode = true;
    private boolean includeTestCode = true;
    private File excludedSourceRoot = null;
    private List<File> excludedSourceRoots = new ArrayList<>();
    private boolean failBuild = true;
    private boolean skip = false;
    private boolean parallel = false;
    private boolean parseFullCompilationUnit = false;

    @Override
    public EnforcerLevel getLevel() {
        return failBuild
                ? EnforcerLevel.ERROR
                : EnforcerLevel.WARN;
    }

    @Override
    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
        try {
            if (isSkip(helper)) {
                LOGGER.info("restrict-imports-enforcer rule is skipped");
                return;
            }

            final MavenProject project = (MavenProject) helper.evaluate("${project}");

            LOGGER.debug("Checking for banned imports");

            final BannedImportGroups groups = createGroupsFromPluginConfiguration();
            LOGGER.debug("Banned import groups:\n{}", groups);

            final AnalyzerSettings analyzerSettings = createAnalyzerSettingsFromPluginConfiguration(helper, project);
            LOGGER.debug("Analyzer settings:\n{}", analyzerSettings);

            final AnalyzeResult analyzeResult = analyzer.analyze(analyzerSettings, groups);
            LOGGER.debug("Analyzer result:\n{}", analyzeResult);

            if (analyzeResult.bannedImportsOrWarningsFound()) {
                final String errorMessage = matchFormatter
                        .formatMatches(analyzerSettings.getAllDirectories(), analyzeResult);

                if (analyzeResult.bannedImportsFoundIn() && isFailBuild(helper)) {
                    throw new EnforcerRuleException(errorMessage);
                } else {
                    LOGGER.warn(errorMessage);
                    LOGGER.warn(
                            "Detected banned imports will not fail the build as the 'failBuild' flag is set to false!");
                }

            } else {
                LOGGER.debug("No banned imports found");
            }
        } catch (final BannedImportDefinitionException e) {
            throw new EnforcerRuleException("RestrictImports rule configuration error: " + e.getMessage(), e);
        } catch (final EnforcerRuleException | RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException("Encountered unexpected exception: " + e.getLocalizedMessage(), e);
        }
    }

    private BannedImportGroups createGroupsFromPluginConfiguration() {
        if (!this.groups.isEmpty()) {
            final List<BannedImportGroup> bannedImportGroups = this.groups.stream()
                    .map(BannedImportGroupDefinition::createGroupFromPluginConfiguration)
                    .collect(Collectors.toList());
            return BannedImportGroups.builder()
                    .withGroups(bannedImportGroups)
                    .build();
        }
        final BannedImportGroup singleGroup = createGroupFromPluginConfiguration();
        return BannedImportGroups.builder()
                .withGroup(singleGroup)
                .build();
    }

    private AnalyzerSettings createAnalyzerSettingsFromPluginConfiguration(
            EnforcerRuleHelper helper,
            MavenProject mavenProject) throws Exception {
        if (!(includeCompileCode || includeTestCode)) {
            throw new IllegalArgumentException("Configuration error: No sources were included");
        }

        final List<Path> excludedSourceRootsAbsolutePaths = excludedSourceRootsAbsolutePaths();
        final Collection<Path> srcDirectories = this.includeCompileCode
                ? listSourceRoots(mavenProject.getCompileSourceRoots(), excludedSourceRootsAbsolutePaths)
                : Collections.emptyList();
        final Collection<Path> testDirectories = this.includeTestCode
                ? listSourceRoots(mavenProject.getTestCompileSourceRoots(), excludedSourceRootsAbsolutePaths)
                : Collections.emptyList();

        final Charset sourceFileCharset = determineSourceFileCharset(mavenProject);
        final boolean parallel = isParallel(helper);
        final boolean parseFullCompilationUnit = this.parseFullCompilationUnit;

        return AnalyzerSettings.builder()
                .withSrcDirectories(srcDirectories)
                .withTestDirectories(testDirectories)
                .withSourceFileCharset(sourceFileCharset)
                .enableParallelAnalysis(parallel)
                .withParseFullCompilationUnit(parseFullCompilationUnit)
                .build();
    }

    private List<Path> excludedSourceRootsAbsolutePaths() {
        final List<File> excludedSourceRoots = excludedSourceRoot != null
                ? Collections.singletonList(excludedSourceRoot)
                : this.excludedSourceRoots;
        return excludedSourceRoots.stream().map(sourceRoot -> sourceRoot.getAbsoluteFile().toPath())
                .collect(Collectors.toList());
    }

    private Charset determineSourceFileCharset(MavenProject mavenProject) {
        final String mavenCharsetName = (String) mavenProject.getProperties().get("project.build.sourceEncoding");
        if (mavenCharsetName != null) {
            return Charset.forName(mavenCharsetName);
        }
        return Charset.defaultCharset();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Collection<Path> listSourceRoots(Collection pathNames, Collection<Path> excludedSourceRootsAbsolutePaths) {
        final Collection<String> pathNamesAsString = pathNames;
        return pathNamesAsString.stream()
                .peek(pathName -> LOGGER.debug("Including source dir: {}", pathName))
                .map(Paths::get)
                .filter(path -> include(path, excludedSourceRootsAbsolutePaths))
                .collect(Collectors.toList());
    }

    private boolean include(Path path, Collection<Path> excludedSourceRootsAbsolutePaths) {
        final boolean exclude = excludedSourceRootsAbsolutePaths.contains(path);
        if (exclude) {
            LOGGER.debug("Excluding source dir: {} according to excludedSourceRoots", path);
            return false;
        }
        return true;
    }

    private void checkGroups(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException("Configuration error: you can either define a list of banned import "
                    + "definitions using <groups> OR define a single banned import definition on top level without "
                    + "<groups> but not both");
        }
    }

    public void setParseFullCompilationUnit(boolean parseFullCompilationUnit) {
        this.parseFullCompilationUnit = parseFullCompilationUnit;
    }

    @Override
    public void setBasePackage(String basePackage) {
        checkGroups(this.groups.isEmpty());
        super.setBasePackage(basePackage);
    }

    @Override
    public void setBasePackages(List<String> basePackages) {
        checkGroups(this.groups.isEmpty());
        super.setBasePackages(basePackages);
    }

    @Override
    public void setBannedImport(String bannedImport) {
        checkGroups(this.groups.isEmpty());
        super.setBannedImport(bannedImport);
    }

    @Override
    public void setBannedImports(List<String> bannedPackages) {
        checkGroups(this.groups.isEmpty());
        super.setBannedImports(bannedPackages);
    }

    @Override
    public void setAllowedImport(String allowedImport) {
        checkGroups(this.groups.isEmpty());
        super.setAllowedImport(allowedImport);
    }

    @Override
    public void setAllowedImports(List<String> allowedImports) {
        checkGroups(this.groups.isEmpty());
        super.setAllowedImports(allowedImports);
    }

    @Override
    public void setExclusion(String exclusion) {
        checkGroups(this.groups.isEmpty());
        super.setExclusion(exclusion);
    }

    @Override
    public void setExclusions(List<String> exclusions) {
        checkGroups(this.groups.isEmpty());
        super.setExclusions(exclusions);
    }

    @Override
    public void setReason(String reason) {
        checkGroups(this.groups.isEmpty());
        super.setReason(reason);
    }

    public void setGroups(List<BannedImportGroupDefinition> groups) {
        checkGroups(!this.hasInput());
        checkArgument(groups != null && !groups.isEmpty(), "Groups may not be empty");
        this.groups = groups;
    }

    public final void setIncludeCompileCode(boolean includeCompileCode) {
        this.includeCompileCode = includeCompileCode;
    }

    public final void setIncludeTestCode(boolean includeTestCode) {
        this.includeTestCode = includeTestCode;
    }

    public final void setExcludedSourceRoot(File excludedSourceRoot) {
        checkArgument(this.excludedSourceRoots.isEmpty(),
                "Configuration error: you should either specify a single excluded source root using <excludedSourceRoot> or multiple "
                        + "excluded source roots using <excludedSourceRoots> but not both");
        checkArgument(this.excludedSourceRoot == null,
                "If you want to specify multiple excluded source root you have to wrap them in a <excludedSourceRoots> tag");
        this.excludedSourceRoot = excludedSourceRoot;
    }

    public final void setExcludedSourceRoots(List<File> excludedSourceRoots) {
        checkArgument(this.excludedSourceRoot == null,
                "Configuration error: you should either specify a single excluded source root using <excludedSourceRoot> or multiple "
                        + "excluded source roots using <excludedSourceRoots> but not both");
        this.excludedSourceRoots = excludedSourceRoots;
    }

    public void setFailBuild(boolean failBuild) {
        this.failBuild = failBuild;
    }

    private boolean isFailBuild(EnforcerRuleHelper evaluator) throws Exception {
        final Object failBuildProperty = evaluator.evaluate("${" + FAIL_BUILD_PROPERTY_NAME + "}");
        if (failBuildProperty != null) {
            LOGGER.warn(
                    "'{}={}' has been passed which takes precedence over 'failBuild={}' configuration in the pom file",
                    FAIL_BUILD_PROPERTY_NAME, failBuildProperty, this.failBuild);
            return "true".equalsIgnoreCase(failBuildProperty.toString());
        }
        return this.failBuild;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    private boolean isSkip(EnforcerRuleHelper evaluator) throws Exception {
        final Object skipProperty = evaluator.evaluate("${" + SKIP_PROPERTY_NAME + "}");
        if (skipProperty != null) {
            LOGGER.warn(
                    "'{}={}' has been passed which takes precedence over 'skip={}' configuration in the pom file",
                    SKIP_PROPERTY_NAME, skipProperty, this.skip);
            return "true".equalsIgnoreCase(skipProperty.toString());
        }
        return this.skip;
    }

    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }

    private boolean isParallel(EnforcerRuleHelper evaluator) throws Exception {
        final Object parallelProperty = evaluator.evaluate("${" + PARALLEL_ANALYSIS_PROPERTY_NAME + "}");
        if (parallelProperty != null) {
            LOGGER.warn(
                    "'{}={}' has been passed which takes precedence over 'parallel={}' configuration in the pom file",
                    PARALLEL_ANALYSIS_PROPERTY_NAME, parallelProperty, this.parallel);
            return "true".equalsIgnoreCase(parallelProperty.toString());
        }
        return this.parallel;
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

}
