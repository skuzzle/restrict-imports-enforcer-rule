package de.skuzzle.enforcer.restrictimports.analyze;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaLineParserTest {

    private static final JavaLineParser subject = new JavaLineParser();

    @Test
    public void testValidImport() {
        assertThat(subject.parseImport("import java.util.List;")).isPresent().get().isEqualTo("java.util.List");
    }

    @Test
    public void testInvalidImport1() {
        assertThat(subject.parseImport("import java.util.List")).isNotPresent();
    }

    @Test
    public void testInvalidImport2() {
        assertThat(subject.parseImport("importjava.util.List;")).isNotPresent();
    }

    @Test
    public void testPackageParse() {
        assertThat(subject.parsePackage("package a.b.c.d;")).isPresent().get().isEqualTo("a.b.c.d");
    }

    @Test
    public void testValidPackageParse() {
        assertThat(subject.parsePackage("package a.b.c.d;")).isPresent().get().isEqualTo("a.b.c.d");
    }

    @Test
    public void testInvalidPackageParse1() {
        assertThat(subject.parsePackage("packagea.b.c.d;")).isNotPresent();
    }

    @Test
    public void testInvalidPackageParse2() {
        assertThat(subject.parsePackage("package a.b.c.d")).isNotPresent();
    }

    @Test
    public void testInvalidPackageParse3() {
        assertThat(subject.parsePackage("")).isNotPresent();
    }
}
