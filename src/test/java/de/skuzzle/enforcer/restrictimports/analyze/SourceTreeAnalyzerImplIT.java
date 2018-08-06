package de.skuzzle.enforcer.restrictimports.analyze;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

public class SourceTreeAnalyzerImplIT {

    private final SourceTreeAnalyzer subject = SourceTreeAnalyzer.getInstance();

    private final FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

    @Test
    public void testFindBannedImportInSingleBasePackage() throws Exception {
        final Path root = fs.getPath("/");
        final Path sourceFile = new SourceFileBuilder()
                .atPath("src/main/java/de/skuzzle/Sample.java")
                .withLines("package de.skuzzle;", "import java.util.ArrayList;");

        final AnalyzeResult analyzeResult = subject.analyze(Stream.of(root),
                BannedImportGroup.builder()
                        .withBasePackages("**")
                        .withBannedImports("java.util.ArrayList")
                        .build());

        final AnalyzeResult expected = AnalyzeResult.builder()
                .withMatches(MatchedFile
                        .forSourceFile(sourceFile)
                        .withMatchAt(2, "java.util.ArrayList"))
                .build();

        assertThat(analyzeResult).isEqualTo(expected);
    }

    @Test
    public void testFindBannedImportInMultipleBasePackages() throws Exception {
        final Path root = fs.getPath("/");
        final Path sourceFile1 = new SourceFileBuilder()
                .atPath("src/main/java/de/skuzzle1/Sample.java")
                .withLines(
                        "package de.skuzzle1;",
                        "import java.util.ArrayList;");
        final Path sourceFile2 = new SourceFileBuilder()
                .atPath("src/main/java/de/skuzzle2/Sample2.java")
                .withLines(
                        "package de.skuzzle2;",
                        "import java.util.ArrayList;");

        final AnalyzeResult analyzeResult = subject.analyze(Stream.of(root),
                BannedImportGroup.builder()
                        .withBasePackages("de.skuzzle1.**", "de.skuzzle2.**")
                        .withBannedImports("java.util.ArrayList")
                        .build());

        final AnalyzeResult expected = AnalyzeResult.builder()
                .withMatches(
                        MatchedFile
                                .forSourceFile(sourceFile1)
                                .withMatchAt(2, "java.util.ArrayList"),
                        MatchedFile.forSourceFile(sourceFile2)
                                .withMatchAt(2,
                                        "java.util.ArrayList"))
                .build();

        assertThat(analyzeResult).isEqualTo(expected);
    }

    @Test
    public void testSkipNonJavaFile() throws Exception {
        final Path root = fs.getPath("/");

        new SourceFileBuilder()
                .atPath("src/main/java/de/skuzzle/Sample.NOT_JAVA")
                .withLines("package de.skuzzle;", "import java.util.ArrayList;");

        final AnalyzeResult analyzeResult = subject.analyze(Stream.of(root),
                BannedImportGroup.builder()
                        .withBasePackages("**")
                        .withBannedImports("java.util.ArrayList")
                        .build());

        assertThat(analyzeResult.bannedImportsFound()).isFalse();
    }

    private class SourceFileBuilder {
        private Path file;

        public SourceFileBuilder atPath(String first)
                throws IOException {

            final String[] parts = first.split("/");

            file = fs.getPath(parts[0], Arrays.copyOfRange(parts, 1, parts.length));
            Files.createDirectories(file.getParent());
            return this;
        }

        public Path withLines(CharSequence... lines) throws IOException {
            final Iterable<? extends CharSequence> it = Arrays.stream(lines)::iterator;
            Files.write(file, it);
            return file.toAbsolutePath();
        }
    }
}
