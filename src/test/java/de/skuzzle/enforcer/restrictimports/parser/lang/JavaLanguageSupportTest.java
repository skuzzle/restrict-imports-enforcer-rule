package de.skuzzle.enforcer.restrictimports.parser.lang;

import static org.assertj.core.api.Assertions.assertThat;

import de.skuzzle.enforcer.restrictimports.parser.ImportStatement;
import org.junit.jupiter.api.Test;

public class JavaLanguageSupportTest {

    private final JavaLanguageSupport subject = new JavaLanguageSupport();

    @Test
    public void testValidImport() {
        assertThat(subject.parseImport("import java.util.List;",1 )).first().isEqualTo(new ImportStatement("java.util.List",1));
    }

    @Test
    public void testInvalidImport1() {
        assertThat(subject.parseImport("import java.util.List",1)).isEmpty();
    }

    @Test
    public void testInvalidImport2() {
        assertThat(subject.parseImport("importjava.util.List;",1)).isEmpty();
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
