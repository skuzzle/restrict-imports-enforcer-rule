package de.skuzzle.enforcer.restrictimports.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ImportStatementParserImplTest {

    private Path tempSourceFile(Path tmpDir, String fileName, Charset charset, String... lines) throws IOException {
        final Path sourceFile = tmpDir.resolve(fileName);
        Files.write(sourceFile, Arrays.asList(lines), charset);
        return sourceFile;
    }

    private Path tempSourceFile(Path tmpDir, String fileName, String... lines) throws IOException {
        return tempSourceFile(tmpDir, fileName, StandardCharsets.UTF_8, lines);
    }

    @Test
    void testAnalyzeFallbackOnFailure(@TempDir Path tempDir) throws IOException {
        final Path sourceFile = tempSourceFile(tempDir, "Filename.java", StandardCharsets.UTF_8,
                "import de.skuzzle.test;",
                "class Filename {"); // compile failure, unclosed '{'

        final boolean parseFullCompilationUnit = true;
        final ImportStatementParser subject = ImportStatementParser.forCharset(StandardCharsets.UTF_8,
                parseFullCompilationUnit);

        final ParsedFile result = subject.parse(sourceFile);
        assertThat(result.getAnnotations()).containsExactly(
                Annotation.withMessage("Failed to parse in full-compilation-unit mode. Analysis might be inaccurate"));
        assertThat(result.getImports()).containsExactly(new ImportStatement("de.skuzzle.test", 1, ImportType.IMPORT));
        assertThat(result.isFailedToParse()).isFalse(); // should only be only true
                                                        // fallback parsing also failed
    }

    @Test
    void testFullQualifiedAnnotationUsage(@TempDir Path tempDir) throws Exception {
        final Path sourceFile = tempSourceFile(tempDir, "Filename.java",
                "class Test {",
                "  @a.b.c.Annotation",
                "  @Override",
                "  String foo(int parameter) {",
                "    return null;",
                "  }",
                "}");
        final boolean parseFullCompilationUnit = true;
        final ImportStatementParser subject = ImportStatementParser.forCharset(StandardCharsets.UTF_8,
                parseFullCompilationUnit);
        final ParsedFile parsedFile = subject.parse(sourceFile);
        assertThat(parsedFile.getImports()).containsOnly(
                new ImportStatement("a.b.c.Annotation", 2, ImportType.QUALIFIED_TYPE_USE));
    }

    @Test
    void testAnalyzeInlineFullQualifiedClassUsage(@TempDir Path tempDir) throws Exception {
        final Path sourceFile = tempSourceFile(tempDir, "Filename.java",
                "import de.skzzle.test;",
                "class Test {",
                "  net.io.Whatever foo(com.foo.bar.Xyz parameter) {",
                "    org.apache.commons.lang.StringUtils.isBlank(\"xyz\");",
                "    boolean foo = abc.test.StringUtils.isBlank(\"xyz\");",
                "    List.of(\"1\").stream().filter(foo.bar.xyz.StringUtils::isBlank);",
                "    Collections.emptyList().stream().filter(Objects::nonNull);",
                "    List<String> list = new java.util.ArrayList<>();",
                "    return null;",
                "  }",
                "}");
        final boolean parseFullCompilationUnit = true;
        final ImportStatementParser subject = ImportStatementParser.forCharset(StandardCharsets.UTF_8,
                parseFullCompilationUnit);
        final ParsedFile parsedFile = subject.parse(sourceFile);
        assertThat(parsedFile.getImports()).containsOnly(
                new ImportStatement("de.skzzle.test", 1, ImportType.IMPORT),
                new ImportStatement("com.foo.bar.Xyz", 3, ImportType.QUALIFIED_TYPE_USE),
                new ImportStatement("net.io.Whatever", 3, ImportType.QUALIFIED_TYPE_USE),
                new ImportStatement("org.apache.commons.lang.StringUtils", 4, ImportType.QUALIFIED_TYPE_USE),
                new ImportStatement("abc.test.StringUtils", 5, ImportType.QUALIFIED_TYPE_USE),
                new ImportStatement("foo.bar.xyz.StringUtils", 6, ImportType.QUALIFIED_TYPE_USE),
                new ImportStatement("java.util.ArrayList", 8, ImportType.QUALIFIED_TYPE_USE));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testAnalyzeWithUmlautsUtf8(boolean parseFullCompilationUnit, @TempDir Path tempDir) throws IOException {
        final Path sourceFile = tempSourceFile(tempDir, "Filename.java", StandardCharsets.UTF_8,
                "import de.sküzzle.test;");

        final ImportStatementParser subject = ImportStatementParser.forCharset(StandardCharsets.UTF_8,
                parseFullCompilationUnit);
        final ParsedFile parsedFile = subject.parse(sourceFile);
        assertThat(parsedFile.getImports()).containsOnly(new ImportStatement("de.sküzzle.test", 1, ImportType.IMPORT));
        assertThat(parsedFile.getFqcn()).isEqualTo("Filename");
        assertThat(parsedFile.getPath()).isEqualTo(sourceFile);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testAnalyzeWithUmlautsIso8859(boolean parseFullCompilationUnit, @TempDir Path tempDir) throws IOException {
        final Path sourceFile = tempSourceFile(tempDir, "Filename.java", StandardCharsets.ISO_8859_1,
                "import de.sküzzle.test;");

        final ImportStatementParser subject = ImportStatementParser.forCharset(StandardCharsets.ISO_8859_1,
                parseFullCompilationUnit);
        final ParsedFile parsedFile = subject.parse(sourceFile);
        assertThat(parsedFile.getImports()).containsOnly(new ImportStatement("de.sküzzle.test", 1, ImportType.IMPORT));
        assertThat(parsedFile.getFqcn()).isEqualTo("Filename");
        assertThat(parsedFile.getPath()).isEqualTo(sourceFile);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testAnalyzeDefaultPackage(boolean parseFullCompilationUnit, @TempDir Path tempDir) throws IOException {
        final Path sourceFile = tempSourceFile(tempDir, "Filename.java", "import de.skuzzle.test;");

        final ImportStatementParser subject = ImportStatementParser.forCharset(StandardCharsets.UTF_8,
                parseFullCompilationUnit);
        final ParsedFile parsedFile = subject.parse(sourceFile);
        assertThat(parsedFile.getImports()).containsOnly(new ImportStatement("de.skuzzle.test", 1, ImportType.IMPORT));
        assertThat(parsedFile.getFqcn()).isEqualTo("Filename");
        assertThat(parsedFile.getPath()).isEqualTo(sourceFile);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testAnalyzeWithPackageDefinition(boolean parseFullCompilationUnit, @TempDir Path tempDir) throws IOException {
        final Path sourceFile = tempSourceFile(tempDir, "Filename.java",
                "package com.foo.bar;",
                "import de.skuzzle.test;");

        final ImportStatementParser subject = ImportStatementParser.forCharset(StandardCharsets.UTF_8,
                parseFullCompilationUnit);

        final ParsedFile parsedFile = subject.parse(sourceFile);
        assertThat(parsedFile.getImports()).containsOnly(new ImportStatement("de.skuzzle.test", 2, ImportType.IMPORT));
        assertThat(parsedFile.getFqcn()).isEqualTo("com.foo.bar.Filename");
        assertThat(parsedFile.getPath()).isEqualTo(sourceFile);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testAnalyzeWithStaticImport(boolean parseFullCompilationUnit, @TempDir Path tempDir) throws IOException {
        final Path sourceFile = tempSourceFile(tempDir, "Filename.java",
                "import static de.skuzzle.test;");
        final ImportStatementParser subject = ImportStatementParser.forCharset(StandardCharsets.UTF_8,
                parseFullCompilationUnit);
        final ParsedFile parsedFile = subject.parse(sourceFile);
        assertThat(parsedFile.getImports())
                .containsOnly(new ImportStatement("de.skuzzle.test", 1, ImportType.STATIC_IMPORT));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testAnalyzeEmptyFile(boolean parseFullCompilationUnit, @TempDir Path tempDir) throws IOException {
        final Path sourceFile = tempSourceFile(tempDir, "Filename.java", "");
        final ImportStatementParser subject = ImportStatementParser.forCharset(StandardCharsets.UTF_8,
                parseFullCompilationUnit);
        final ParsedFile parsedFile = subject.parse(sourceFile);
        assertThat(parsedFile.getFqcn()).isEqualTo("Filename");
        assertThat(parsedFile.getPath()).isEqualTo(sourceFile);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testStopAtClassDeclaration(boolean parseFullCompilationUnit, @TempDir Path tempDir) throws IOException {
        final Path sourceFile = tempSourceFile(tempDir, "Filename.java",
                "package com.foo.bar;",
                "",
                "import de.skuzzle.test;",
                "import de.skuzzle.test2;",
                "",
                "public class HereStartsAClass {",
                "}");
        final ImportStatementParser subject = ImportStatementParser.forCharset(StandardCharsets.UTF_8,
                parseFullCompilationUnit);

        final ParsedFile parsedFile = subject.parse(sourceFile);
        assertThat(parsedFile.getImports()).containsOnly(
                new ImportStatement("de.skuzzle.test", 3, ImportType.IMPORT),
                new ImportStatement("de.skuzzle.test2", 4, ImportType.IMPORT));
        assertThat(parsedFile.getFqcn()).isEqualTo("com.foo.bar.Filename");
        assertThat(parsedFile.getPath()).isEqualTo(sourceFile);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testStopAtClassDeclarationWithAnnotations(boolean parseFullCompilationUnit, @TempDir Path tempDir)
            throws IOException {
        final Path sourceFile = tempSourceFile(tempDir, "Filename.java",
                "package com.foo.bar;",
                "",
                "import de.skuzzle.test;",
                "import de.skuzzle.test2;",
                "@Deprecated",
                "public class HereStartsAClass {",
                "}");
        final ImportStatementParser subject = ImportStatementParser.forCharset(StandardCharsets.UTF_8,
                parseFullCompilationUnit);

        final ParsedFile parsedFile = subject.parse(sourceFile);
        assertThat(parsedFile.getImports()).containsOnly(
                new ImportStatement("de.skuzzle.test", 3, ImportType.IMPORT),
                new ImportStatement("de.skuzzle.test2", 4, ImportType.IMPORT));
        assertThat(parsedFile.getFqcn()).isEqualTo("com.foo.bar.Filename");
        assertThat(parsedFile.getPath()).isEqualTo(sourceFile);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testLeadingAndTrailingWhitespaces(boolean parseFullCompilationUnit, @TempDir Path tempDir) throws IOException {
        final Path sourceFile = tempSourceFile(tempDir, "Filename.java",
                "package com.foo.bar;  ",
                "",
                "\t    ",
                "import de.skuzzle.test;  \t",
                "import de.skuzzle.test2;    ",
                "\t\t",
                "\tpublic class HereStartsAClass {",
                "}");
        final ImportStatementParser subject = ImportStatementParser.forCharset(StandardCharsets.UTF_8,
                parseFullCompilationUnit);

        final ParsedFile parsedFile = subject.parse(sourceFile);
        assertThat(parsedFile.getImports()).containsOnly(
                new ImportStatement("de.skuzzle.test", 4, ImportType.IMPORT),
                new ImportStatement("de.skuzzle.test2", 5, ImportType.IMPORT));
        assertThat(parsedFile.getFqcn()).isEqualTo("com.foo.bar.Filename");
        assertThat(parsedFile.getPath()).isEqualTo(sourceFile);
    }
}
