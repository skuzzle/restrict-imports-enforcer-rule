package de.skuzzle.restrictimports.gradle;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import de.skuzzle.enforcer.restrictimports.analyze.AnalyzeResult;
import de.skuzzle.enforcer.restrictimports.analyze.AnalyzerSettings;
import de.skuzzle.enforcer.restrictimports.analyze.BannedImportGroup;
import de.skuzzle.enforcer.restrictimports.analyze.BannedImportGroups;
import de.skuzzle.enforcer.restrictimports.analyze.NotFixable;
import de.skuzzle.enforcer.restrictimports.analyze.PackagePattern;
import de.skuzzle.enforcer.restrictimports.analyze.SourceTreeAnalyzer;
import de.skuzzle.enforcer.restrictimports.formatting.MatchFormatter;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;

public abstract class RestrictImports extends DefaultTask
        implements BannedImportGroupDefinition, RestrictImportsTaskConfiguration {

    public static final String DEFAULT_TASK_NAME = "defaultRestrictImports";

    private final SourceTreeAnalyzer analyzer = SourceTreeAnalyzer.getInstance();
    private final MatchFormatter matchFormatter = MatchFormatter.getInstance();

    @Inject
    protected abstract ObjectFactory getObjectFactory();

    @Input
    public abstract Property<Boolean> getIncludeCompileCode();

    @Input
    public abstract Property<Boolean> getIncludeTestCode();

    @Input
    public abstract Property<Boolean> getParallel();

    @Input
    public abstract Property<Boolean> getFailBuild();

    @Input
    public abstract Property<Boolean> getParseFullCompilationUnit();

    @InputFiles
    @SkipWhenEmpty
    public abstract Property<FileCollection> getMainSourceSet();

    @InputFiles
    @SkipWhenEmpty
    public abstract Property<FileCollection> getTestSourceSet();

    @Nested
    public abstract ListProperty<BannedImportGroupDefinition> getGroups();

    @Nested
    public abstract ListProperty<NotFixableDefinition> getNotFixable();

    public RestrictImports() {
        Conventions.apply(getProject(), this);
        getBasePackages().convention(Collections.singletonList("**"));
    }

    private boolean isDefaultRestrictImportsTask() {
        return DEFAULT_TASK_NAME.equals(getName());
    }

    @TaskAction
    public void restrictImports() {
        if (isDefaultRestrictImportsTask() && (!getBannedImports().isPresent() || getBannedImports().get().isEmpty())
                && (!getGroups().isPresent() || getGroups().get().isEmpty())) {
            getLogger().debug("Skipping default restrictImports task because no banned imports were defined");
            return;
        }
        getLogger().debug("Checking for banned imports");

        final BannedImportGroups groups = createGroupsFromPluginConfiguration();
        getLogger().debug("Banned import groups: {}", groups);

        final AnalyzerSettings analyzerSettings = createAnalyzerSettingsFromPluginConfiguration();
        getLogger().debug("Analyzer settings: {}", analyzerSettings);

        final AnalyzeResult analyzeResult = analyzer.analyze(analyzerSettings, groups);
        getLogger().debug("Analyzer result: {}", analyzeResult);

        if (analyzeResult.bannedImportsOrWarningsFound()) {
            final String errorMessage = matchFormatter.formatMatches(analyzeResult);

            if (analyzeResult.bannedImportsFound() && getFailBuild().get()) {
                throw new RestrictedImportsFoundException(errorMessage);
            } else {
                getLogger().warn(errorMessage);
                getLogger().warn(
                        "Detected banned imports will not fail the build as the 'failBuild' flag is set to false!");
            }
        } else {
            getLogger().debug("No banned imports found");
        }
    }

    private BannedImportGroups createGroupsFromPluginConfiguration() {
        final List<NotFixable> globalNotFixables = getNotFixable().get().stream()
                .map(definition -> NotFixable.of(
                        PackagePattern.parse(definition.getIn().get()),
                        PackagePattern.parseAll(definition.getAllowedImports().get())))
                .collect(toList());

        if (!getGroups().isPresent() || getGroups().get().isEmpty()) {
            return BannedImportGroups.builder()
                    .withGroup(createGroupFromPluginConfiguration(this, globalNotFixables)).build();
        }
        final List<BannedImportGroup> bannedImportGroups = getGroups().get().stream()
                .map(definition -> createGroupFromPluginConfiguration(definition, globalNotFixables)).collect(toList());
        return BannedImportGroups.builder()
                .withGroups(bannedImportGroups)
                .build();
    }

    public BannedImportGroup createGroupFromPluginConfiguration(BannedImportGroupDefinition definition,
            List<NotFixable> globalNotFixables) {
        return BannedImportGroup.builder()
                .withBasePackages(PackagePattern.parseAll(definition.getBasePackages().getOrElse(singletonList("**"))))
                .withBannedImports(PackagePattern.parseAll(definition.getBannedImports().getOrElse(emptyList())))
                .withAllowedImports(PackagePattern.parseAll(definition.getAllowedImports().getOrElse(emptyList())))
                .withExclusions(PackagePattern.parseAll(definition.getExclusions().getOrElse(emptyList())))
                .withReason(definition.getReason().getOrElse(""))
                .withNotFixables(globalNotFixables)
                .build();
    }

    private AnalyzerSettings createAnalyzerSettingsFromPluginConfiguration() {
        final FileCollection main = getMainSourceSet().get();

        final List<Path> srcDirectories = getIncludeCompileCode().get()
                ? main.getFiles().stream().map(File::toPath).collect(toList())
                : emptyList();

        final FileCollection test = getTestSourceSet().get();
        final List<Path> testDirectories = getIncludeTestCode().get()
                ? test.getFiles().stream().map(File::toPath).collect(toList())
                : emptyList();

        // TODO: find sourceset charset
        final Charset charset = StandardCharsets.UTF_8;

        return AnalyzerSettings.builder()
                .withSrcDirectories(srcDirectories)
                .withTestDirectories(testDirectories)
                .withSourceFileCharset(charset)
                .withParseFullCompilationUnit(getParseFullCompilationUnit().get())
                .enableParallelAnalysis(getParallel().get())
                .build();
    }

    public void group(Action<BannedImportGroupDefinition> definition) {
        final BannedImportGroupDefinition groupDefinition = getObjectFactory()
                .newInstance(BannedImportGroupDefinition.class);
        groupDefinition.getBasePackages().convention(Collections.singletonList("**"));

        definition.execute(groupDefinition);
        getGroups().add(groupDefinition);
    }

    public void notFixable(Action<NotFixableDefinition> definition) {
        final NotFixableDefinition notFixableInstance = getObjectFactory().newInstance(NotFixableDefinition.class);
        definition.execute(notFixableInstance);
        getNotFixable().add(notFixableInstance);
    }
}
