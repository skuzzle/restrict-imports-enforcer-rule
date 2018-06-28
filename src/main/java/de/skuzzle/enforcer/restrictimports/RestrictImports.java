package de.skuzzle.enforcer.restrictimports;

import static com.google.common.base.Preconditions.checkArgument;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import de.skuzzle.enforcer.restrictimports.api.AnalyzerFactory;
import de.skuzzle.enforcer.restrictimports.api.SourceTreeAnalyzer;
import de.skuzzle.enforcer.restrictimports.model.BannedImportGroup;
import de.skuzzle.enforcer.restrictimports.model.Match;
import de.skuzzle.enforcer.restrictimports.model.PackagePattern;

/**
 * Enforcer rule which restricts the usage of certain packages or classes within a Java
 * code base.
 */
public class RestrictImports implements EnforcerRule {

    private PackagePattern basePackage = PackagePattern.parse("**");
    private List<PackagePattern> basePackages = new ArrayList<>();

    private PackagePattern bannedImport = null;
    private List<PackagePattern> bannedImports = new ArrayList<>();

    private PackagePattern allowedImport = null;
    private List<PackagePattern> allowedImports = new ArrayList<>();

    private PackagePattern exclusion = null;
    private List<PackagePattern> exclusions = new ArrayList<>();

    private boolean includeTestCode;
    private String reason;

    private static final SourceTreeAnalyzer ANALYZER = AnalyzerFactory
            .getInstance()
            .createAnalyzer();

    @Override
    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
        final Log log = helper.getLog();
        try {
            final MavenProject project = (MavenProject) helper.evaluate("${project}");

            log.debug("Checking for banned imports");
            final BannedImportGroup group = new BannedImportGroup(
                    assembleList(this.basePackage, this.basePackages),
                    assembleList(this.bannedImport, this.bannedImports),
                    assembleList(this.allowedImport, this.allowedImports),
                    assembleList(this.exclusion, this.exclusions),
                    this.reason);

            checkConsistency(group);

            final Collection<Path> sourceRoots = listSourceRoots(project, log)
                    .collect(Collectors.toList());

            final Map<String, List<Match>> matches = ANALYZER.analyze(
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

    private void checkConsistency(BannedImportGroup group) throws EnforcerRuleException {
        ANALYZER.checkGroupConsistency(group);
    }

    private List<PackagePattern> assembleList(PackagePattern single,
            List<PackagePattern> multi) {
        if (single == null) {
            return multi;
        } else {
            final List<PackagePattern> result = new ArrayList<>(multi);
            result.add(single);
            return result;
        }
    }

    private static String relativize(Collection<Path> roots, String path) {
        for (final Path root : roots) {
            final String absoluteRoot = root.toAbsolutePath().toString();
            if (path.startsWith(absoluteRoot)) {
                return path.substring(absoluteRoot.length());
            }
        }
        return path;
    }

    private String formatErrorString(Collection<Path> roots, BannedImportGroup group,
            Map<String, List<Match>> groups) {
        final StringBuilder b = new StringBuilder("\nBanned imports detected:\n");
        final String message = group.getReason();
        if (message != null && !message.isEmpty()) {
            b.append("Reason: ").append(message).append("\n");
        }
        groups.forEach((fileName, matches) -> {
            b.append("\tin file: ")
                    .append(relativize(roots, fileName))
                    .append("\n");
            matches.forEach(match -> appendMatch(match, b));
        });
        return b.toString();
    }

    private void appendMatch(Match match, StringBuilder b) {
        b.append("\t\t")
                .append(match.getMatchedString())
                .append(" (Line: ")
                .append(match.getImportLine())
                .append(")\n");
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
        this.basePackage = PackagePattern.parse(basePackage);
    }

    public final void setBasePackages(List<String> basePackages) {
        checkArgument(basePackages != null && !basePackages.isEmpty(),
                "bannedPackages must not be empty");
        this.basePackages = PackagePattern.parseAll(basePackages);
    }

    public void setBannedImport(String bannedImport) {
        this.bannedImport = PackagePattern.parse(bannedImport);
    }

    public final void setBannedImports(List<String> bannedPackages) {
        checkArgument(bannedPackages != null && !bannedPackages.isEmpty(),
                "bannedPackages must not be empty");
        this.bannedImport = null;
        this.bannedImports = PackagePattern.parseAll(bannedPackages);
    }

    public final void setAllowedImport(String allowedImport) {
        this.allowedImport = PackagePattern.parse(allowedImport);
    }

    public final void setAllowedImports(List<String> allowedImports) {
        this.allowedImports = PackagePattern.parseAll(allowedImports);
    }

    public final void setExclusion(String exclusion) {
        this.exclusion = PackagePattern.parse(exclusion);
    }

    public final void setExclusions(List<String> exclusions) {
        this.exclusions = PackagePattern.parseAll(exclusions);
    }

    public final void setIncludeTestCode(boolean includeTestCode) {
        this.includeTestCode = includeTestCode;
    }

    public final void setReason(String reason) {
        this.reason = reason;
    }
}