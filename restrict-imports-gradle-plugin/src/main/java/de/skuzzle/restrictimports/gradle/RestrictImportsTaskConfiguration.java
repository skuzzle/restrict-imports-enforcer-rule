package de.skuzzle.restrictimports.gradle;

import org.gradle.api.Action;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.SkipWhenEmpty;

public interface RestrictImportsTaskConfiguration {

    DirectoryProperty getReportsDirectory();

    @Input
    Property<Boolean> getIncludeCompileCode();

    @Input
    Property<Boolean> getIncludeTestCode();

    @Input
    Property<Boolean> getFailBuild();

    @Input
    Property<Boolean> getParallel();

    @Input
    Property<Boolean> getParseFullCompilationUnit();

    @InputFiles
    @SkipWhenEmpty
    Property<FileCollection> getMainSourceSet();

    @InputFiles
    @SkipWhenEmpty
    Property<FileCollection> getTestSourceSet();

    @Input
    ListProperty<BannedImportGroupDefinition> getGroups();

    @Input
    ListProperty<NotFixableDefinition> getNotFixable();

    void group(Action<BannedImportGroupDefinition> definition);

    void notFixable(Action<NotFixableDefinition> definition);
}
