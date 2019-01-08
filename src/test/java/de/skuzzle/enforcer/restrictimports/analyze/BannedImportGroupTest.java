package de.skuzzle.enforcer.restrictimports.analyze;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class BannedImportGroupTest {

    @Test
    public void testEquals() throws Exception {
        EqualsVerifier.forClass(BannedImportGroup.class).verify();
    }

    @Test
    void testNoBannedImports() throws Exception {
        assertThatExceptionOfType(BannedImportDefinitionException.class)
                .isThrownBy(() -> BannedImportGroup.builder().build());
    }

    @Test
    void testInconsistentAllowedImports() throws Exception {
        assertThatExceptionOfType(BannedImportDefinitionException.class)
                .isThrownBy(() -> BannedImportGroup.builder()
                        .withBannedImports("dont.care")
                        .withBasePackages("com.foo.*")
                        .withAllowedImports("foo.**")
                        .build());

    }

    @Test
    void testAmbiguousBasePackage() throws Exception {
        assertThatExceptionOfType(BannedImportDefinitionException.class)
                .isThrownBy(() -> BannedImportGroup.builder()
                        .withBasePackages("java.util.**", "java.util.text.**")
                        .withBannedImports("any.thing")
                        .build())
                .withMessageContaining("There are ambiguous base package definitions: java.util.**, java.util.text.**");

    }

    @Test
    void testAmbiguousExclusion() throws Exception {
        assertThatExceptionOfType(BannedImportDefinitionException.class)
                .isThrownBy(() -> BannedImportGroup.builder()
                        .withBannedImports("any.thing")
                        .withExcludedClasses("java.util.**", "java.util.text.**")
                        .build())
                .withMessageContaining("There are ambiguous exclusion definitions: java.util.**, java.util.text.**");
    }

    @Test
    void testAmbiguousBans() throws Exception {
        assertThatExceptionOfType(BannedImportDefinitionException.class)
                .isThrownBy(() -> BannedImportGroup.builder()
                        .withBannedImports("java.util.**", "java.util.text.**")
                        .build())
                .withMessageContaining(
                        "There are ambiguous banned import definitions: java.util.**, java.util.text.**");
    }

    @Test
    void testInconsistentExclusions() throws Exception {
        assertThatExceptionOfType(BannedImportDefinitionException.class)
                .isThrownBy(() -> BannedImportGroup.builder()
                        .withBannedImports("dont.care")
                        .withBasePackages("base.package.**")
                        .withAllowedImports("foo.bar.**")
                        .build())
                .withMessageContaining("The allowed import pattern 'foo.bar.**'");
    }

    @Test
    void testExclusionsMustMatchBasePattern() throws Exception {
        assertThatExceptionOfType(BannedImportDefinitionException.class)
                .isThrownBy(() -> BannedImportGroup.builder()
                        .withBasePackages("de.skuzzle.**")
                        .withBannedImports("foo.bar")
                        .withExcludedClasses("de.not.skuzzle.**")
                        .build())
                .withMessageContaining("The exclusion pattern 'de.not.skuzzle.**'");
    }

    @Test
    void testBasePackageMustNotBeStatic() throws Exception {
        assertThatExceptionOfType(BannedImportDefinitionException.class)
                .isThrownBy(() -> BannedImportGroup.builder()
                        .withBasePackages("static de.skuzzle.Foo")
                        .withBannedImports("foo.bar")
                        .build())
                .withMessageContaining("Base packages must not be static");
    }

    @Test
    void testExclusionMustNotBeStatic() throws Exception {
        assertThatExceptionOfType(BannedImportDefinitionException.class)
                .isThrownBy(() -> BannedImportGroup.builder()
                        .withBasePackages("de.skuzzle.**")
                        .withBannedImports("foo.bar")
                        .withExcludedClasses("static de.skuzzle.Foo")
                        .build())
                .withMessageContaining("Exclusions must not be static");
    }

}
