package de.skuzzle.enforcer.restrictimports.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class WhitespacesTest {
    @Test
    void testTrimLeading() throws Exception {
        assertThat(Whitespaces.trimAll("  \t\r\ntest test")).isEqualTo("test test");
    }

    @Test
    void testTrimTrailing() throws Exception {
        assertThat(Whitespaces.trimAll("test test\t\n\r    ")).isEqualTo("test test");
    }

    @Test
    void testTrimLeadingTrailing() throws Exception {
        assertThat(Whitespaces.trimAll("  \t\r\ntest test\t\n\r    ")).isEqualTo("test test");
    }
}
