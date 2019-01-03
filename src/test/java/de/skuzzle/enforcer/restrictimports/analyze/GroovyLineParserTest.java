package de.skuzzle.enforcer.restrictimports.analyze;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GroovyLineParserTest {

    private static final GroovyLineParser subject = new GroovyLineParser();

    @Test
    public void testValidImport1() {
        assertThat(subject.parseImport("import java.util.List;")).isPresent().get().isEqualTo("java.util.List");
    }

    @Test
    public void testValidImport2() {
        assertThat(subject.parseImport("import java.util.List")).isPresent().get().isEqualTo("java.util.List");
    }

    @Test
    public void testInvalidImport2() {
        assertThat(subject.parseImport("importjava.util.List")).isNotPresent();
    }

    @Test
    public void testValidPackageParse1() {
        assertThat(subject.parsePackage("package a.b.c.d")).isPresent().get().isEqualTo("a.b.c.d");
    }

    @Test
    public void testValidPackageParse2() {
        assertThat(subject.parsePackage("package a.b.c.d;")).isPresent().get().isEqualTo("a.b.c.d");
    }

    @Test
    public void testInvalidPackageParse1() {
        assertThat(subject.parsePackage("packagea.b.c.d;")).isNotPresent();
    }

    @Test
    public void testInvalidPackageParse2() {
        assertThat(subject.parsePackage("")).isNotPresent();
    }
}
