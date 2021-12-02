package de.skuzzle.enforcer.restrictimports.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ImportStatementParserImplTest {

    private final Path path = mock(Path.class);
    private final Path fileName = mock(Path.class);

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
        final ParsedFile parsedFile = subject.parse(path);
        assertThat(parsedFile.getImports()).containsOnly(new ImportStatement("de.skuzzle.test", 1, false));
        assertThat(parsedFile.getFqcn()).isEqualTo("Filename");
        assertThat(parsedFile.getPath()).isEqualTo(path);
    }

    @Test
    void testAnalyzeWithPackageDefinition() {
        final ImportStatementParserImpl subject = new ImportStatementParserImpl(lines(
                "package com.foo.bar;",
                "import de.skuzzle.test;"));
        final ParsedFile parsedFile = subject.parse(path);
        assertThat(parsedFile.getImports()).containsOnly(new ImportStatement("de.skuzzle.test", 2, false));
        assertThat(parsedFile.getFqcn()).isEqualTo("com.foo.bar.Filename");
        assertThat(parsedFile.getPath()).isEqualTo(path);
    }

    @Test
    void testAnalyzeWithStaticImport() {
        final ImportStatementParserImpl subject = new ImportStatementParserImpl(lines(
                "import static de.skuzzle.test;"));
        final ParsedFile parsedFile = subject.parse(path);
        assertThat(parsedFile.getImports()).containsOnly(new ImportStatement("de.skuzzle.test", 1, true));
    }

    @Test
    void testAnalyzeEmptyFile() {
        final ImportStatementParserImpl subject = new ImportStatementParserImpl(lines(""));
        final ParsedFile parsedFile = subject.parse(path);
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
        final ParsedFile parsedFile = subject.parse(path);
        assertThat(parsedFile.getImports()).containsOnly(
                new ImportStatement("de.skuzzle.test", 3, false),
                new ImportStatement("de.skuzzle.test2", 4, false));
        assertThat(parsedFile.getFqcn()).isEqualTo("com.foo.bar.Filename");
        assertThat(parsedFile.getPath()).isEqualTo(path);
    }

    @Test
    void testStopAtClassDeclarationWithAnnotations() {
        final ImportStatementParserImpl subject = new ImportStatementParserImpl(lines(
                "package com.foo.bar;",
                "",
                "import de.skuzzle.test;",
                "import de.skuzzle.test2;",
                "@Deprecated",
                "public class HereStartsAClass {",
                "}"));
        final ParsedFile parsedFile = subject.parse(path);
        assertThat(parsedFile.getImports()).containsOnly(
                new ImportStatement("de.skuzzle.test", 3, false),
                new ImportStatement("de.skuzzle.test2", 4, false));
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
        final ParsedFile parsedFile = subject.parse(path);
        assertThat(parsedFile.getImports()).containsOnly(
                new ImportStatement("de.skuzzle.test", 4, false),
                new ImportStatement("de.skuzzle.test2", 5, false));
        assertThat(parsedFile.getFqcn()).isEqualTo("com.foo.bar.Filename");
        assertThat(parsedFile.getPath()).isEqualTo(path);
    }
}
