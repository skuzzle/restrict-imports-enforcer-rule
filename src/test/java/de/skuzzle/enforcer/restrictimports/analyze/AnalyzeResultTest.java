package de.skuzzle.enforcer.restrictimports.analyze;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class AnalyzeResultTest {

    @Test
    public void testEquals() throws Exception {
        EqualsVerifier.forClass(AnalyzeResult.class).verify();
    }
}
