package de.skuzzle.enforcer.restrictimports.parser;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class ParsedFileTest {

    @Test
    public void testEquals() {
        EqualsVerifier.forClass(ParsedFile.class).verify();
    }

    @Test
    public void testEqualsImportStatement() {
        EqualsVerifier.forClass(ImportStatement.class).verify();
    }
}
