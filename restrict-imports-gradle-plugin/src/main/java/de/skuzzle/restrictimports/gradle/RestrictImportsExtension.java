package de.skuzzle.restrictimports.gradle;

import java.util.Collections;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.model.ObjectFactory;

public abstract class RestrictImportsExtension
        implements BannedImportGroupDefinition, RestrictImportsTaskConfiguration {

    public static final String NAME = "restrictImports";

    @Inject
    protected abstract ObjectFactory getObjectFactory();

    @Override
    public void group(Action<BannedImportGroupDefinition> spec) {
        final BannedImportGroupDefinition groupDefinition = getObjectFactory()
                .newInstance(BannedImportGroupDefinition.class);
        groupDefinition.getBasePackages().convention(Collections.singletonList("**"));

        spec.execute(groupDefinition);
        getGroups().add(groupDefinition);
    }

    @Override
    public void notFixable(Action<NotFixableDefinition> spec) {
        final NotFixableDefinition notFixableInstance = getObjectFactory().newInstance(NotFixableDefinition.class);
        spec.execute(notFixableInstance);
        getNotFixable().add(notFixableInstance);
    }

}
