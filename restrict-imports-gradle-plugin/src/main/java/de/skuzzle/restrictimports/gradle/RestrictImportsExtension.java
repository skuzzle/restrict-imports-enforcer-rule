package de.skuzzle.restrictimports.gradle;

import java.util.Collections;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;

public abstract class RestrictImportsExtension
        implements BannedImportGroupDefinition, RestrictImportsTaskConfiguration {

    public static final String NAME = "restrictImports";

    @Inject
    protected abstract ObjectFactory getObjectFactory();

    @Override
    public void group(Action<BannedImportGroupDefinition> definition) {
        final BannedImportGroupDefinition groupDefinition = getObjectFactory()
                .newInstance(BannedImportGroupDefinition.class);
        groupDefinition.getBasePackages().convention(Collections.singletonList("**"));

        definition.execute(groupDefinition);
        getGroups().add(groupDefinition);
    }

    @Override
    public void notFixable(Action<NotFixableDefinition> definition) {
        final NotFixableDefinition notFixableInstance = getObjectFactory().newInstance(NotFixableDefinition.class);
        definition.execute(notFixableInstance);
        getNotFixable().add(notFixableInstance);
    }

}
