package de.skuzzle.restrictimports.gradle;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;

public interface NotFixableDefinition {

    @Input
    Property<String> getIn();

    @Input
    ListProperty<String> getAllowedImports();

    @Internal
    Property<String> getBecause();
}
