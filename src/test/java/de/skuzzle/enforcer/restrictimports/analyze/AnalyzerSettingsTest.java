package de.skuzzle.enforcer.restrictimports.analyze;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class AnalyzerSettingsTest {

    @Test
    void testEquals() throws Exception {
        EqualsVerifier.forClass(AnalyzerSettings.class)
                // o_O
                .withPrefabValues(Charset.class, StandardCharsets.ISO_8859_1,
                        StandardCharsets.UTF_8)
                .verify();
    }
}
