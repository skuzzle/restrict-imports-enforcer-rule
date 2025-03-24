package de.skuzzle.enforcer.restrictimports.analyze;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class BannedImportGroupsTest {

    @Test
    void testEquals() throws Exception {
        EqualsVerifier.forClass(BannedImportGroups.class).verify();
    }

    @Test
    void testSingleAmbiguousBasePackages() throws Exception {
        assertThatExceptionOfType(BannedImportDefinitionException.class)
                .isThrownBy(() -> BannedImportGroups.builder().withGroup(BannedImportGroup.builder()
                        .withBasePackages("java.util.**")
                        .withBannedImports("any.thing"))
                        .withGroup(BannedImportGroup.builder()
                                .withBasePackages("java.util.**")
                                .withBannedImports("any.thing"))
                        .build())
                .withMessageContaining("There are two groups with equal base package definitions: java.util.**");
    }

    @Test
    void testMultipleAmbiguousBasePackagesWithDifferentOrder() throws Exception {
        assertThatExceptionOfType(BannedImportDefinitionException.class)
                .isThrownBy(() -> BannedImportGroups.builder().withGroup(BannedImportGroup.builder()
                        .withBasePackages("java.util.**", "com.foo.*")
                        .withBannedImports("any.thing"))
                        .withGroup(BannedImportGroup.builder()
                                .withBasePackages("com.foo.*", "java.util.**")
                                .withBannedImports("any.thing"))
                        .build())
                .withMessageContaining(
                        "There are two groups with equal base package definitions: com.foo.*, java.util.**");
    }
}
