package de.skuzzle.enforcer.restrictimports.parser.lang;

import de.skuzzle.enforcer.restrictimports.parser.ImportStatement;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KotlinGroovyLanguageSupportTest {

    private final KotlinGroovyLanguageSupport subject = new KotlinGroovyLanguageSupport();

    @Test
    public void testValidImport1() {
        assertThat(subject.parseImport("import java.util.List;", 1)).first()
                .isEqualTo(new ImportStatement("java.util.List", 1));
    }

    @Test
    public void testValidImport2() {
        assertThat(subject.parseImport("import java.util.List", 1)).first()
                .isEqualTo(new ImportStatement("java.util.List", 1));
    }

    @Test
    void testAliasedImport() throws Exception {
        assertThat(subject.parseImport("import java.util.List as NewList", 1)).first()
                .isEqualTo(new ImportStatement("java.util.List", 1));
    }

    @Test
    void testMultipleImportsInSameLine() {
        assertThat(subject.parseImport("import java.util.List; import java.util.Collection;", 1)).containsOnly(
                new ImportStatement("java.util.List", 1),
                new ImportStatement("java.util.Collection", 1));
    }

    @Test
    void testMultipleImportsInSameLineWithAlias() {
        assertThat(subject.parseImport("import java.util.List as Set; import java.util.Collection;", 1)).containsOnly(
                new ImportStatement("java.util.List", 1),
                new ImportStatement("java.util.Collection", 1));
    }

    @Test
    void testMultipleImportsInSameLineWithAliasNoSemicolonAtEnd() {
        assertThat(subject.parseImport("import java.util.List as Set; import java.util.Collection", 2)).containsOnly(
                new ImportStatement("java.util.List", 2),
                new ImportStatement("java.util.Collection", 2));
    }

    @Test
    public void testInvalidImport2() {
        assertThat(subject.parseImport("importjava.util.List", 1)).isEmpty();
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

    @Test
    void testDanglingSemicolonSingleImport() throws Exception {
        assertThat(subject.parseImport("import java.util.List; ;;", 1)).containsOnly(
                new ImportStatement("java.util.List", 1));
    }

    @Test
    void testDanglingSemicolonMultipleImports() throws Exception {
        assertThat(subject.parseImport("import java.util.List;import java.util.Collection;;", 1)).containsOnly(
                new ImportStatement("java.util.List", 1),
                new ImportStatement("java.util.Collection", 1));
    }
}
