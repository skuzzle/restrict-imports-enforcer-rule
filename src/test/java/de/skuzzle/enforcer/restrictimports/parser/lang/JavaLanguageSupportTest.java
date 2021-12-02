package de.skuzzle.enforcer.restrictimports.parser.lang;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import de.skuzzle.enforcer.restrictimports.parser.ImportStatement;

public class JavaLanguageSupportTest {

    private final JavaLanguageSupport subject = new JavaLanguageSupport();

    @Test
    public void testValidImport() {
        assertThat(subject.parseImport("import java.util.List;", 1)).first()
                .isEqualTo(new ImportStatement("java.util.List", 1, false));
    }

    @Test
    public void testValidStaticImport() {
        assertThat(subject.parseImport("import static java.util.List.of;", 1)).first()
                .isEqualTo(new ImportStatement("java.util.List.of", 1, true));
    }

    @Test
    public void testInvalidImport1() {
        assertThat(subject.parseImport("import java.util.List", 1)).isEmpty();
    }

    @Test
    public void testInvalidImport2() {
        assertThat(subject.parseImport("importjava.util.List;", 1)).isEmpty();
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

    @Test
    void testMultipleImportsInSameLine() {
        assertThat(subject.parseImport("import java.util.List; import java.util.Collection;", 1)).containsOnly(
                new ImportStatement("java.util.List", 1, false),
                new ImportStatement("java.util.Collection", 1, false));
    }

    @Test
    void testMultipleImportsWithStaticInSameLine() {
        assertThat(subject.parseImport("import java.util.List; import static java.util.Collections.emptyList;", 1))
                .containsOnly(
                        new ImportStatement("java.util.List", 1, false),
                        new ImportStatement("java.util.Collections.emptyList", 1, true));
    }

    @Test
    void testDanglingSemicolonSingleImport() throws Exception {
        assertThat(subject.parseImport("import java.util.List; ;;", 1)).containsOnly(
                new ImportStatement("java.util.List", 1, false));
    }

    @Test
    void testDanglingSemicolonMultipleImports() throws Exception {
        assertThat(subject.parseImport("import java.util.List;import java.util.Collection;;", 1)).containsOnly(
                new ImportStatement("java.util.List", 1, false),
                new ImportStatement("java.util.Collection", 1, false));
    }
}
