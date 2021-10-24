package de.skuzzle.enforcer.restrictimports.rule;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated in favor of {@link org.apache.maven.plugins.enforcer.RestrictImports}.
 */
@Deprecated
public class RestrictImports extends org.apache.maven.plugins.enforcer.RestrictImports {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestrictImports.class);

    @Override
    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
        LOGGER.warn(
                "You are using a deprecated declaration of the RestrictImports enforcer rule (Deprecated since 1.4.0). "
                        + "Instead of "
                        + "<restrictImports implementation=\"de.skuzzle.enforcer.restrictimports.rule.RestrictImports\"> you "
                        + "can now simplify the decalration to just <RestrictImports>. The deprecated declaration "
                        + "format will be removed with the next major release (2.x.x)");
        super.execute(helper);
    }

}
