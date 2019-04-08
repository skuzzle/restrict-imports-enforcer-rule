package de.skuzzle.enforcer.restrictimports.parser;

import de.skuzzle.enforcer.restrictimports.parser.lang.JavaLanguageSupport;
import de.skuzzle.enforcer.restrictimports.parser.lang.LanguageSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImportStatementParserTest {

    private final Path path = mock(Path.class);
    private final Path fileName = mock(Path.class);
    private final LanguageSupport javaLang = new JavaLanguageSupport();

    @BeforeEach
    void setup() {
        when(path.getFileName()).thenReturn(fileName);
        when(fileName.toString()).thenReturn("Filename.java");
    }

    private LineSupplier lines(String...lines) {
        return path -> Arrays.stream(lines);
    }

    @Test
    void testAnalyzeDefaultPackage() {
        final ImportStatementParser subject = new ImportStatementParser(lines("import de.skuzzle.test;"));
        final ParsedFile parsedFile = subject.analyze(path, javaLang);
        assertThat(parsedFile.getImports()).containsOnly(new ImportStatement("de.skuzzle.test", 1));
        assertThat(parsedFile.getFqcn()).isEqualTo("Filename");
        assertThat(parsedFile.getPath()).isEqualTo(path);
    }

    @Test
    void testAnalyzeWithPackageDefinition() {
        final ImportStatementParser subject = new ImportStatementParser(lines(
                "package com.foo.bar;",
                "import de.skuzzle.test;"));
        final ParsedFile parsedFile = subject.analyze(path, javaLang);
        assertThat(parsedFile.getImports()).containsOnly(new ImportStatement("de.skuzzle.test", 2));
        assertThat(parsedFile.getFqcn()).isEqualTo("com.foo.bar.Filename");
        assertThat(parsedFile.getPath()).isEqualTo(path);
    }

    @Test
    void testStopAtClassDeclaration() {
        final ImportStatementParser subject = new ImportStatementParser(lines(
                "package com.foo.bar;",
                "",
                "import de.skuzzle.test;",
                "import de.skuzzle.test2;",
                "",
                "public class HereStartsAClass {",
                "}"));
        final ParsedFile parsedFile = subject.analyze(path, javaLang);
        assertThat(parsedFile.getImports()).containsOnly(
                new ImportStatement("de.skuzzle.test", 3),
                new ImportStatement("de.skuzzle.test2", 4));
        assertThat(parsedFile.getFqcn()).isEqualTo("com.foo.bar.Filename");
        assertThat(parsedFile.getPath()).isEqualTo(path);
    }
}
