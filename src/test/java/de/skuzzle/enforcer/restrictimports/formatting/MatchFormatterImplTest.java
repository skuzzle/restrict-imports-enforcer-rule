package de.skuzzle.enforcer.restrictimports.formatting;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

import de.skuzzle.enforcer.restrictimports.analyze.AnalyzeResult;
import de.skuzzle.enforcer.restrictimports.analyze.BannedImportGroup;
import de.skuzzle.enforcer.restrictimports.analyze.MatchedFile;
import de.skuzzle.enforcer.restrictimports.analyze.PackagePattern;

public class MatchFormatterImplTest {

    private final MatchFormatter subject = MatchFormatter.getInstance();

    @Test
    public void testFormatWithReason() throws Exception {
        final BannedImportGroup group = BannedImportGroup.builder()
                .withBasePackages("**")
                .withBannedImports("java.util.*")
                .withReason("Some reason")
                .build();

        final URL resourceDirUrl = getClass().getResource("/SampleJavaFile.java");
        final File resourceDirFile = new File(resourceDirUrl.toURI());
        final Path root = resourceDirFile.toPath().getParent();
        final Path sourceFile = root.resolve("SampleJavaFile.java");
        final Collection<Path> roots = ImmutableList.of(root);

        final AnalyzeResult analyzeResult = AnalyzeResult.builder()
                .withMatches(MatchedFile.forSourceFile(sourceFile)
                        .matchedBy(group)
                        .withMatchAt(3, "java.util.ArrayList",
                                PackagePattern.parse("java.util.*")))
                .build();

        final String formatted = subject.formatMatches(roots, analyzeResult);

        assertThat(formatted).isEqualTo("\nBanned imports detected:\n" +
                "Reason: Some reason\n" +
                "\tin file: SampleJavaFile.java\n" +
                "\t\tjava.util.ArrayList (Line: 3, Matched by: java.util.*)\n");
    }
}
