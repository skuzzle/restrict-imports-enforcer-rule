package de.skuzzle.restrictimports.gradle;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

public interface BannedImportGroupDefinition {

    @Input
    @Optional
    ListProperty<String> getBasePackages();

    @Input
    ListProperty<String> getBannedImports();

    @Input
    @Optional
    ListProperty<String> getAllowedImports();

    @Input
    @Optional
    ListProperty<String> getExclusions();

    @Input
    @Optional
    Property<String> getReason();
}
