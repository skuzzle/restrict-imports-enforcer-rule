package de.skuzzle.enforcer.restrictimports.parser;

import de.skuzzle.enforcer.restrictimports.parser.lang.JavaLanguageSupport;
import de.skuzzle.enforcer.restrictimports.parser.lang.LanguageSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImportStatementParserImplTest {

    private final Path path = mock(Path.class);
    private final Path fileName = mock(Path.class);
    private final LanguageSupport javaLang = new JavaLanguageSupport();

    @BeforeEach
    void setup() {
        when(path.getFileName()).thenReturn(fileName);
        when(fileName.toString()).thenReturn("Filename.java");
    }

    private LineSupplier lines(String... lines) {
        return path -> Arrays.stream(lines);
    }

    @Test
    void testAnalyzeDefaultPackage() {
        final ImportStatementParserImpl subject = new ImportStatementParserImpl(lines("import de.skuzzle.test;"));
        final ParsedFile parsedFile = subject.parse(path, javaLang);
        assertThat(parsedFile.getImports()).containsOnly(new ImportStatement("de.skuzzle.test", 1));
        assertThat(parsedFile.getFqcn()).isEqualTo("Filename");
        assertThat(parsedFile.getPath()).isEqualTo(path);
    }

    @Test
    void testAnalyzeWithPackageDefinition() {
        final ImportStatementParserImpl subject = new ImportStatementParserImpl(lines(
                "package com.foo.bar;",
                "import de.skuzzle.test;"));
        final ParsedFile parsedFile = subject.parse(path, javaLang);
        assertThat(parsedFile.getImports()).containsOnly(new ImportStatement("de.skuzzle.test", 2));
        assertThat(parsedFile.getFqcn()).isEqualTo("com.foo.bar.Filename");
        assertThat(parsedFile.getPath()).isEqualTo(path);
    }

    @Test
    void testAnalyzeWithStaticImport() {
        final ImportStatementParserImpl subject = new ImportStatementParserImpl(lines(
                "import static de.skuzzle.test;"));
        final ParsedFile parsedFile = subject.parse(path, javaLang);
        assertThat(parsedFile.getImports()).containsOnly(new ImportStatement("static de.skuzzle.test", 1));
    }

    @Test
    void testAnalyzeEmptyFile() {
        final ImportStatementParserImpl subject = new ImportStatementParserImpl(lines(""));
        final ParsedFile parsedFile = subject.parse(path, javaLang);
        assertThat(parsedFile.getFqcn()).isEqualTo("Filename");
        assertThat(parsedFile.getPath()).isEqualTo(path);
    }

    @Test
    void testStopAtClassDeclaration() {
        final ImportStatementParserImpl subject = new ImportStatementParserImpl(lines(
                "package com.foo.bar;",
                "",
                "import de.skuzzle.test;",
                "import de.skuzzle.test2;",
                "",
                "public class HereStartsAClass {",
                "}"));
        final ParsedFile parsedFile = subject.parse(path, javaLang);
        assertThat(parsedFile.getImports()).containsOnly(
                new ImportStatement("de.skuzzle.test", 3),
                new ImportStatement("de.skuzzle.test2", 4));
        assertThat(parsedFile.getFqcn()).isEqualTo("com.foo.bar.Filename");
        assertThat(parsedFile.getPath()).isEqualTo(path);
    }

    @Test
    void testLeadingAndTrailingWhitespaces() {
        final ImportStatementParserImpl subject = new ImportStatementParserImpl(lines(
                "package com.foo.bar;  ",
                "",
                "\t    ",
                "import de.skuzzle.test;  \t",
                "import de.skuzzle.test2;    ",
                "\t\t",
                "\tpublic class HereStartsAClass {",
                "}"));
        final ParsedFile parsedFile = subject.parse(path, javaLang);
        assertThat(parsedFile.getImports()).containsOnly(
                new ImportStatement("de.skuzzle.test", 4),
                new ImportStatement("de.skuzzle.test2", 5));
        assertThat(parsedFile.getFqcn()).isEqualTo("com.foo.bar.Filename");
        assertThat(parsedFile.getPath()).isEqualTo(path);
    }
}
