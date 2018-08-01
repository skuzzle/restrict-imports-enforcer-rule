package de.skuzzle.enforcer.restrictimports.analyze;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class MatchFormatterImplTest {

    private final MatchFormatter subject = MatchFormatter.getInstance();

    @Test
    public void testFormatWithReason() throws Exception {
        final BannedImportGroup group = BannedImportGroup.builder()
                .withBasePackages(ImmutableList.of(PackagePattern.parse("**")))
                .withBannedImports(ImmutableList.of(PackagePattern.parse("foo.bar.**")))
                .withReason("Some reason")
                .build();

        final URL resourceDirUrl = getClass().getResource("/SampleJavaFile.java");
        final File resourceDirFile = new File(resourceDirUrl.toURI());
        final Path root = resourceDirFile.toPath().getParent();
        final Collection<Path> roots = ImmutableList.of(root);

        final Map<Path, List<Match>> matchesPerFile = ImmutableMap
                .of(root.resolve("SampleJavaFile.java"), ImmutableList
                        .of(new Match(root.resolve("SampleJavaFile.java"), 3,
                                "java.util.ArrayList")));

        final String formatted = subject.formatMatches(roots, matchesPerFile, group);

        assertEquals("\nBanned imports detected:\n" +
                "Reason: Some reason\n" +
                "\tin file: SampleJavaFile.java\n" +
                "\t\tjava.util.ArrayList (Line: 3)\n", formatted);
    }
}
