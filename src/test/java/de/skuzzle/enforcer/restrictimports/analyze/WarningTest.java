package de.skuzzle.enforcer.restrictimports.analyze;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

class WarningTest {

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Warning.class).verify();
    }
}
