package de.skuzzle.enforcer.restrictimports.analyze;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class MatchedImportTest {

    @Test
    public void testEquals() throws Exception {
        EqualsVerifier.forClass(MatchedImport.class).verify();
    }
}
