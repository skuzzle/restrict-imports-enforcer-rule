package de.skuzzle.enforcer.restrictimports.parser;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class AnnotationTest {

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Annotation.class).verify();
    }
}
