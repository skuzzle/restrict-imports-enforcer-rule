package de.skuzzle.enforcer.restrictimports.parser;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class ParsedFileTest {

    @Test
    public void testEquals() throws Exception {
        EqualsVerifier.forClass(ParsedFile.class).verify();
    }

    @Test
    public void testEqualsImportStatement() throws Exception {
        EqualsVerifier.forClass(ImportStatement.class).verify();
    }
}
