package de.skuzzle.enforcer.restrictimports.analyze;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class WarningTest {


    @Test
    void testEquals() {
        EqualsVerifier.forClass(Warning.class).verify();
    }
}
