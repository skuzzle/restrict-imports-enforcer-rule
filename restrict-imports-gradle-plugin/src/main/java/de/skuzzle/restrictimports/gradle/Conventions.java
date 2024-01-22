package de.skuzzle.restrictimports.gradle;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

final class Conventions {

    static void apply(Project project, RestrictImportsTaskConfiguration taskConfiguration, String taskName) {
        final ProviderFactory providers = project.getProviders();

        final SourceSetContainer sourceSets = (SourceSetContainer) project.getProperties().get("sourceSets");

        final Provider<FileCollection> main = sourceSets.named(SourceSet.MAIN_SOURCE_SET_NAME).map(SourceSet::getJava)
                .map(SourceDirectorySet::getSourceDirectories);
        final Provider<FileCollection> test = sourceSets.named(SourceSet.TEST_SOURCE_SET_NAME).map(SourceSet::getJava)
                .map(SourceDirectorySet::getSourceDirectories);

        taskConfiguration.getParallel().convention(
                providers.systemProperty("restrictImports.parallel").map("true"::equals)
                        .orElse(true));
        taskConfiguration.getFailBuild().convention(
                providers.systemProperty("restrictImports.failBuild").map("true"::equals)
                        .orElse(true));

        final String reportsDirName = taskName.equals(RestrictImports.DEFAULT_TASK_NAME) ? "" : capitalize(taskName);
        taskConfiguration.getReportsDirectory()
                .convention(project.getLayout().getBuildDirectory().dir("restrictImports" + reportsDirName));

        taskConfiguration.getIncludeCompileCode().convention(true);
        taskConfiguration.getIncludeTestCode().convention(true);
        taskConfiguration.getParseFullCompilationUnit().convention(false);
        taskConfiguration.getMainSourceSet().convention(main);
        taskConfiguration.getTestSourceSet().convention(test);
    }

    private static String capitalize(String s) {
        return s.isEmpty()
                ? ""
                : s.length() == 1
                        ? Character.toUpperCase(s.charAt(0)) + ""
                        : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    static void wire(RestrictImportsTaskConfiguration from, RestrictImportsTaskConfiguration to) {
        to.getReportsDirectory().set(from.getReportsDirectory());
        to.getParallel().set(from.getParallel());
        to.getFailBuild().set(from.getFailBuild());
        to.getIncludeCompileCode().set(from.getIncludeCompileCode());
        to.getIncludeTestCode().set(from.getIncludeTestCode());
        to.getParseFullCompilationUnit().set(from.getParseFullCompilationUnit());
        to.getMainSourceSet().set(from.getMainSourceSet());
        to.getTestSourceSet().set(from.getTestSourceSet());
        to.getGroups().set(from.getGroups());
        to.getNotFixable().set(from.getNotFixable());
    }
}
