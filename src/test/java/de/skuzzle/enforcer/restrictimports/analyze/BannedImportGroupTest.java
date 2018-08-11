package de.skuzzle.enforcer.restrictimports.analyze;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class BannedImportGroupTest {

    @Test
    public void testEquals() throws Exception {
        EqualsVerifier.forClass(BannedImportGroup.class).verify();
    }

    @Test
    void testNoBannedImports() throws Exception {
        assertThatExceptionOfType(EnforcerRuleException.class)
                .isThrownBy(() -> BannedImportGroup.builder().build());
    }

    @Test
    void testInconsistentAllowedImports() throws Exception {
        assertThatExceptionOfType(EnforcerRuleException.class)
                .isThrownBy(() -> BannedImportGroup.builder()
                        .withBannedImports("dont.care")
                        .withBasePackages("com.foo.*")
                        .withAllowedImports("foo.**")
                        .build());

    }

    @Test
    void testInconsistentExclusions() throws Exception {
        assertThatExceptionOfType(EnforcerRuleException.class)
                .isThrownBy(() -> BannedImportGroup.builder()
                        .withBannedImports("dont.care")
                        .withBasePackages("base.package.**")
                        .withAllowedImports("foo.bar.**")
                        .build());
    }

    @Test
    void testExclusionsMustMatchBasePattern() throws Exception {
        assertThatExceptionOfType(EnforcerRuleException.class)
                .isThrownBy(() -> BannedImportGroup.builder()
                        .withBasePackages("de.skuzzle.**")
                        .withBannedImports("foo.bar")
                        .withExcludedClasses("de.not.skuzzle.**")
                        .build());
    }

}
