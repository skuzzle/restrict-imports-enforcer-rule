package de.skuzzle.enforcer.restrictimports;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

/**
 */
public class RestrictImports implements EnforcerRule {

    private List<String> bannedImports = new ArrayList<>();

    private List<String> allowedImports = new ArrayList<>();

    private final ImportMatcher matcher = new ImportMatcherImpl();

    @Override
    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
        final Log log = helper.getLog();
        try {
            final MavenProject project = (MavenProject) helper.evaluate("${project}");

            final List<PackagePattern> bannedPatterns = compile(this.bannedImports);
            final List<PackagePattern> allowedPatterns = compile(this.allowedImports);

            final Map<String, List<Match>> matches = listSourceFiles(project)
                .peek(sourceFile -> log.debug(
                        "Analyzing '" + sourceFile.toString() +"' for banned imports"))
                .flatMap(matchFile(bannedPatterns, allowedPatterns))
                .collect(Collectors.groupingBy(Match::getSourceFile));


            if (!matches.isEmpty()) {
                throw new EnforcerRuleException(formatErrorString(matches));
            }

        } catch (final RuntimeIOException e) {
            throw new EnforcerRuleException("Encountered IO exception", e);
        } catch (final ExpressionEvaluationException e) {
            throw new EnforcerRuleException("Unable to lookup an expression " +
                e.getLocalizedMessage(), e);
        }
    }

    private String formatErrorString(Map<String, List<Match>> groups) {
        final StringBuilder b = new StringBuilder("\nBanned imports detected:\n");
        groups.forEach((fileName, matches) -> {
            b.append("\tin file: ").append(fileName).append("\n");
            matches.forEach(match -> {
                b.append("\t\t").append(match.getMatchedString())
                .append(" (Line: ").append(match.getImportLine()).append(")\n");
            });
        });
        return b.toString();
    }

    private Function<Path, Stream<Match>> matchFile(Collection<PackagePattern> banned,
            Collection<PackagePattern> allowed) {
        return sourceFile -> this.matcher.matchFile(sourceFile, banned, allowed);
    }

    private static boolean isJavaSourceFile(Path path) {
        return !Files.isDirectory(path) &&
            path.getFileName().toString().toLowerCase().endsWith(".java");
    }

    private static Stream<Path> listFiles(Path dir) {
        try {
            return Files.list(dir);
        } catch (final IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Stream<Path> listSourceFiles(MavenProject project) {
        final List<String> roots = project.getCompileSourceRoots();
        return roots.stream()
                .map(Paths::get)
                .flatMap(RestrictImports::listFiles)
                .filter(RestrictImports::isJavaSourceFile);
    }

    private List<PackagePattern> compile(List<String> patterns) {
        return patterns.stream().map(PackagePattern::parse).collect(Collectors.toList());
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

    public void setAllowedImports(List<String> allowedImports) {
        this.allowedImports = allowedImports;
    }

    public void setBannedImports(List<String> bannedImports) {
        this.bannedImports = bannedImports;
    }
}