package de.skuzzle.enforcer.restrictimports;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;

/**
 * Enforcer rule which restricts the usage of certain packages or classes within a Java
 * code base.
 *
 * @deprecated Use {@link de.skuzzle.enforcer.restrictimports.rule.RestrictImports}
 *             instead.
 * @author Simon Taddiken
 */
@Deprecated
public class RestrictImports
        extends de.skuzzle.enforcer.restrictimports.rule.RestrictImports {

    @Override
    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
        throw new EnforcerRuleException(String.format("%nDeprecation warning (since 0.12.0):%n" +
                "You are using the deprecated RestrictImports rule from '%s'. Please use the class '%s' instead",
                this.getClass().getName(),
                de.skuzzle.enforcer.restrictimports.rule.RestrictImports.class.getName()));
    }

}