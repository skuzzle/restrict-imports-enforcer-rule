package de.skuzzle.enforcer.restrictimports.analyze;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.junit.jupiter.api.Test;

public class GroupConsistencyCheckerImplTest {

    @Test
    public void testNoBannedImports() throws Exception {
        assertThatExceptionOfType(EnforcerRuleException.class)
                .isThrownBy(() -> BannedImportGroup.builder().build());
    }

    @Test
    public void testInconsistentAllowedImports() throws Exception {
        assertThatExceptionOfType(EnforcerRuleException.class)
                .isThrownBy(() -> {
                    BannedImportGroup.builder()
                            .withBannedImports("dont.care")
                            .withBasePackages("com.foo.*")
                            .withAllowedImports("foo.**")
                            .build();
                });

    }

    @Test
    public void testInconsistentExclusions() throws Exception {
        assertThatExceptionOfType(EnforcerRuleException.class)
                .isThrownBy(() -> {
                    BannedImportGroup.builder()
                            .withBannedImports("dont.care")
                            .withBasePackages("base.package.**")
                            .withAllowedImports("foo.bar.**")
                            .build();
                });
    }
}
