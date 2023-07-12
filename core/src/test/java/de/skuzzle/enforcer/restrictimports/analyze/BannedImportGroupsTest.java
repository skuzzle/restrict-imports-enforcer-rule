package de.skuzzle.enforcer.restrictimports.analyze;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class BannedImportGroupsTest {

    @Test
    void testEquals() throws Exception {
        EqualsVerifier.forClass(BannedImportGroups.class).verify();
    }
}
