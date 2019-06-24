package de.skuzzle.enforcer.restrictimports.formatting;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

import de.skuzzle.enforcer.restrictimports.analyze.AnalyzeResult;
import de.skuzzle.enforcer.restrictimports.analyze.BannedImportGroup;
import de.skuzzle.enforcer.restrictimports.analyze.MatchedFile;
import de.skuzzle.enforcer.restrictimports.analyze.PackagePattern;

public class MatchFormatterImplTest {

    private URL resourceDirUrl;
    private File resourceDirFile;
    private Path root;
    private Path sourceFile;
    private Collection<Path> roots;
    private BannedImportGroup group;

    private final MatchFormatter subject = MatchFormatter.getInstance();

    @BeforeEach
    void setup() throws URISyntaxException {
        resourceDirUrl = getClass().getResource("/SampleJavaFile.java");
        resourceDirFile = new File(resourceDirUrl.toURI());
        root = resourceDirFile.toPath().getParent();
        sourceFile = root.resolve("SampleJavaFile.java");
        roots = ImmutableList.of(root);
        group = BannedImportGroup.builder()
                .withBasePackages("**")
                .withBannedImports("java.util.*")
                .withReason("Some reason")
                .build();
    }

    @Test
    public void testFormatMatchInCompileAndTestCode() throws Exception {
        final AnalyzeResult analyzeResult = AnalyzeResult.builder()
                .withDuration(5000)
                .withMatches(MatchedFile.forSourceFile(sourceFile)
                        .matchedBy(group)
                        .withMatchAt(3, "java.util.ArrayList", PackagePattern.parse("java.util.*")))
                .withMatchesInTestCode(MatchedFile.forSourceFile(sourceFile)
                        .matchedBy(group)
                        .withMatchAt(3, "java.util.ArrayList", PackagePattern.parse("java.util.*")))
                .build();

        final String formatted = subject.formatMatches(roots, analyzeResult);

        assertThat(formatted).isEqualTo("\nBanned imports detected:\n\n" +
                "Reason: Some reason\n" +
                "\tin file: SampleJavaFile.java\n" +
                "\t\tjava.util.ArrayList (Line: 3, Matched by: java.util.*)\n" +
                "\nBanned imports detected in TEST code:\n\n" +
                "Reason: Some reason\n" +
                "\tin file: SampleJavaFile.java\n" +
                "\t\tjava.util.ArrayList (Line: 3, Matched by: java.util.*)\n\n" +
                "Analysis took 5 seconds\n");
    }

    @Test
    public void testFormatMatchInCompileCode() throws Exception {
        final AnalyzeResult analyzeResult = AnalyzeResult.builder()
                .withDuration(5000)
                .withMatches(MatchedFile.forSourceFile(sourceFile)
                        .matchedBy(group)
                        .withMatchAt(3, "java.util.ArrayList", PackagePattern.parse("java.util.*")))
                .build();

        final String formatted = subject.formatMatches(roots, analyzeResult);

        assertThat(formatted).isEqualTo("\nBanned imports detected:\n\n" +
                "Reason: Some reason\n" +
                "\tin file: SampleJavaFile.java\n" +
                "\t\tjava.util.ArrayList (Line: 3, Matched by: java.util.*)\n\n" +
                "Analysis took 5 seconds\n");
    }

    @Test
    public void testFormatMatchInTestCode() throws Exception {
        final AnalyzeResult analyzeResult = AnalyzeResult.builder()
                .withDuration(5000)
                .withMatchesInTestCode(MatchedFile.forSourceFile(sourceFile)
                        .matchedBy(group)
                        .withMatchAt(3, "java.util.ArrayList", PackagePattern.parse("java.util.*")))
                .build();

        final String formatted = subject.formatMatches(roots, analyzeResult);

        assertThat(formatted).isEqualTo("\nBanned imports detected in TEST code:\n\n" +
                "Reason: Some reason\n" +
                "\tin file: SampleJavaFile.java\n" +
                "\t\tjava.util.ArrayList (Line: 3, Matched by: java.util.*)\n\n" +
                "Analysis took 5 seconds\n");
    }
}
