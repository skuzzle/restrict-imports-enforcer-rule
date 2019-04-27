package de.skuzzle.enforcer.restrictimports.parser;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

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
