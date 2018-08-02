package de.skuzzle.enforcer.restrictimports.analyze;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class BannedImportGroupTest {

    @Test
    public void testEquals() throws Exception {
        EqualsVerifier.forClass(BannedImportGroup.class).verify();
    }
}
