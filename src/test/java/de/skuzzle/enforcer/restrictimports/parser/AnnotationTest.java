package de.skuzzle.enforcer.restrictimports.parser;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class AnnotationTest {

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Annotation.class).verify();
    }
}
