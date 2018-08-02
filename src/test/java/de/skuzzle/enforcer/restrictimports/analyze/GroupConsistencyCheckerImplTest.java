package de.skuzzle.enforcer.restrictimports.analyze;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.junit.Test;

public class GroupConsistencyCheckerImplTest {

    @Test(expected = EnforcerRuleException.class)
    public void testNoBannedImports() throws Exception {
        BannedImportGroup.builder().build();
    }

    @Test(expected = EnforcerRuleException.class)
    public void testInconsistentAllowedImports() throws Exception {
        BannedImportGroup.builder()
                .withBannedImports("dont.care")
                .withBasePackages("com.foo.*")
                .withAllowedImports("foo.**")
                .build();

    }

    @Test(expected = EnforcerRuleException.class)
    public void testInconsistentExclusions() throws Exception {
        BannedImportGroup.builder()
                .withBannedImports("dont.care")
                .withBasePackages("base.package.**")
                .withAllowedImports("foo.bar.**")
                .build();
    }
}
