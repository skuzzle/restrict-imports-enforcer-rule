package de.skuzzle.enforcer.restrictimports.formatting;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

import de.skuzzle.enforcer.restrictimports.analyze.AnalyzeResult;
import de.skuzzle.enforcer.restrictimports.analyze.BannedImportGroup;
import de.skuzzle.enforcer.restrictimports.analyze.MatchedFile;
import de.skuzzle.enforcer.restrictimports.analyze.PackagePattern;

import org.junit.jupiter.api.Test;

public class MatchFormatterImplTest {

    private static Path getResource(String resource) {
        try {
            return new File(MatchFormatterImplTest.class.getResource(resource).toURI()).toPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private final Path sourceFile = getResource("/src/main/java/SampleJavaFile.java");

    private final Collection<Path> roots = Collections.singleton(sourceFile.getParent());
    private final BannedImportGroup group = BannedImportGroup.builder()
            .withBasePackages("**")
            .withBannedImports("java.util.*")
            .withReason("Some reason")
            .build();

    private final MatchFormatter subject = MatchFormatter.getInstance();

    @Test
    public void testFormatMatchInCompileAndTestCode() {
        final AnalyzeResult analyzeResult = AnalyzeResult.builder()
                .withDuration(5000)
                .withMatches(MatchedFile.forSourceFile(sourceFile)
                        .matchedBy(group)
                        .withMatchAt(3, "java.util.ArrayList", PackagePattern.parse("java.util.*")))
                .withMatchesInTestCode(MatchedFile.forSourceFile(sourceFile)
                        .matchedBy(group)
                        .withMatchAt(3, "java.util.ArrayList", PackagePattern.parse("java.util.*"))
                        .withMatchAt(4, "java.util.Date", PackagePattern.parse("java.util.*")))
                .withAnalysedFileCount(2)
                .build();

        final String formatted = subject.formatMatches(roots, analyzeResult);

        assertThat(formatted).isEqualTo("\nBanned imports detected:\n\n" +
                "Reason: Some reason\n" +
                "\tin file: SampleJavaFile.java\n" +
                "\t\tjava.util.ArrayList \t(Line: 3, Matched by: java.util.*)\n" +
                "\nBanned imports detected in TEST code:\n\n" +
                "Reason: Some reason\n" +
                "\tin file: SampleJavaFile.java\n" +
                "\t\tjava.util.ArrayList \t(Line: 3, Matched by: java.util.*)\n" +
                "\t\tjava.util.Date      \t(Line: 4, Matched by: java.util.*)\n\n" +
                "Analysis of 2 files took 5 seconds\n");
    }

    @Test
    public void testFormatMatchInCompileCode() {
        final AnalyzeResult analyzeResult = AnalyzeResult.builder()
                .withDuration(5000)
                .withMatches(MatchedFile.forSourceFile(sourceFile)
                        .matchedBy(group)
                        .withMatchAt(3, "java.util.ArrayList", PackagePattern.parse("java.util.*")))
                .withAnalysedFileCount(2)
                .build();

        final String formatted = subject.formatMatches(roots, analyzeResult);

        assertThat(formatted).isEqualTo("\nBanned imports detected:\n\n" +
                "Reason: Some reason\n" +
                "\tin file: SampleJavaFile.java\n" +
                "\t\tjava.util.ArrayList \t(Line: 3, Matched by: java.util.*)\n\n" +
                "Analysis of 2 files took 5 seconds\n");
    }

    @Test
    public void testFormatMatchInTestCode() {
        final AnalyzeResult analyzeResult = AnalyzeResult.builder()
                .withDuration(5000)
                .withMatchesInTestCode(MatchedFile.forSourceFile(sourceFile)
                        .matchedBy(group)
                        .withMatchAt(3, "java.util.ArrayList", PackagePattern.parse("java.util.*")))
                .withAnalysedFileCount(1)
                .build();

        final String formatted = subject.formatMatches(roots, analyzeResult);

        assertThat(formatted).isEqualTo("\nBanned imports detected in TEST code:\n\n" +
                "Reason: Some reason\n" +
                "\tin file: SampleJavaFile.java\n" +
                "\t\tjava.util.ArrayList \t(Line: 3, Matched by: java.util.*)\n\n" +
                "Analysis of 1 file took 5 seconds\n");
    }
}
