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
                        .build())
                .withMessageContaining("The allowed import pattern 'foo.bar.**'");
    }

    @Test
    void testExclusionsMustMatchBasePattern() throws Exception {
        assertThatExceptionOfType(EnforcerRuleException.class)
                .isThrownBy(() -> BannedImportGroup.builder()
                        .withBasePackages("de.skuzzle.**")
                        .withBannedImports("foo.bar")
                        .withExcludedClasses("de.not.skuzzle.**")
                        .build())
                .withMessageContaining("The exclusion pattern 'de.not.skuzzle.**'");
    }

    @Test
    void testBasePackageMustNotBeStatic() throws Exception {
        assertThatExceptionOfType(EnforcerRuleException.class)
                .isThrownBy(() -> BannedImportGroup.builder()
                        .withBasePackages("static de.skuzzle.Foo")
                        .withBannedImports("foo.bar")
                        .build())
                .withMessageContaining("Base packages must not be static");
    }

    @Test
    void testExclusionMustNotBeStatic() throws Exception {
        assertThatExceptionOfType(EnforcerRuleException.class)
                .isThrownBy(() -> BannedImportGroup.builder()
                        .withBasePackages("de.skuzzle.**")
                        .withBannedImports("foo.bar")
                        .withExcludedClasses("static de.skuzzle.Foo")
                        .build())
                .withMessageContaining("Exclusions must not be static");
    }

}
