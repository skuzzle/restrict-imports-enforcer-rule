package de.skuzzle.restrictimports.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskCollection;

import java.util.Collections;

public abstract class RestrictImportsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project target) {
        final RestrictImportsExtension extension = target.getExtensions().create(RestrictImportsExtension.NAME,
                RestrictImportsExtension.class);
        Conventions.apply(target, extension);

        extension.getBasePackages().convention(Collections.singletonList("**"));

        final RestrictImports defaultTaks = target.getTasks().create(RestrictImports.DEFAULT_TASK_NAME,
                RestrictImports.class);
        Conventions.wire(extension, defaultTaks);

        defaultTaks.getBasePackages().set(extension.getBasePackages());
        defaultTaks.getBannedImports().set(extension.getBannedImports());
        defaultTaks.getAllowedImports().set(extension.getAllowedImports());
        defaultTaks.getExclusions().set(extension.getExclusions());
        defaultTaks.getReason().set(extension.getReason());

        final TaskCollection<RestrictImports> allRestrictImportsTasks = target.getTasks()
                .withType(RestrictImports.class);
        target.getTasks().register("restrictImports").configure(it -> {
            it.dependsOn(allRestrictImportsTasks);
        });

    }
}
