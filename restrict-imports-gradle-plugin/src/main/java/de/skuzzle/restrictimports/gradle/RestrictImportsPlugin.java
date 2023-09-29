package de.skuzzle.restrictimports.gradle;

import java.util.Collections;

import javax.inject.Inject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskProvider;

public abstract class RestrictImportsPlugin implements Plugin<Project> {

    @Inject
    protected abstract ProviderFactory getProviders();

    @Override
    public void apply(Project target) {
        final RestrictImportsExtension extension = target.getExtensions().create(RestrictImportsExtension.NAME,
            RestrictImportsExtension.class);
        Conventions.apply(target, extension);

        extension.getBasePackages().convention(Collections.singletonList("**"));

        final RestrictImports task = target.getTasks().create(RestrictImports.DEFAULT_TASK_NAME, RestrictImports.class);
        Conventions.wire(extension, task);

        task.getBasePackages().set(extension.getBasePackages());
        task.getBannedImports().set(extension.getBannedImports());
        task.getAllowedImports().set(extension.getAllowedImports());
        task.getExclusions().set(extension.getExclusions());
        task.getReason().set(extension.getReason());

        final TaskCollection<RestrictImports> allRestrictTasks = target.getTasks().withType(RestrictImports.class);
        target.getTasks().register("restrictImports").configure(it -> {
            it.dependsOn(allRestrictTasks);
        });

    }
}
