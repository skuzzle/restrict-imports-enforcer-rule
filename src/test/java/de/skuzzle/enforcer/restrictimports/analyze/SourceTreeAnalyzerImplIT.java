package de.skuzzle.enforcer.restrictimports.analyze;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import de.skuzzle.enforcer.restrictimports.analyze.BannedImportGroup.Builder;

public class SourceTreeAnalyzerImplIT {

    private final FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
    private final Path root = fs.getPath("/");

    private final AnalyzerSettings settings = AnalyzerSettings.builder()
            .withRootDirectories(root)
            .build();

    @Test
    void testFindBannedImportInSingleBasePackage() throws Exception {
        final Path sourceFile = new SourceFileBuilder(fs)
                .atPath("src/main/java/de/skuzzle/Sample.java")
                .withLines("package de.skuzzle;", "import java.util.ArrayList;");

        final SourceTreeAnalyzer subject = SourceTreeAnalyzer.getInstance();
        final BannedImportGroup group = BannedImportGroup.builder()
                .withBasePackages("**")
                .withBannedImports("java.util.ArrayList")
                .build();
        final BannedImportGroups groups = BannedImportGroups.builder().withGroup(group).build();
        final AnalyzeResult analyzeResult = subject.analyze(settings, groups);

        final PackagePattern expectedMatchedBy = PackagePattern
                .parse("java.util.ArrayList");
        final AnalyzeResult expected = AnalyzeResult.builder()
                .withMatches(MatchedFile
                        .forSourceFile(sourceFile)
                        .matchedBy(group)
                        .withMatchAt(2, "java.util.ArrayList", expectedMatchedBy))
                .build();

        assertThat(analyzeResult).isEqualTo(expected);
    }

    @Test
    void testFindBannedImportOnCorrectLineWithSkippedInlineComment() throws Exception {
        final Path sourceFile = new SourceFileBuilder(fs)
                .atPath("src/main/java/de/skuzzle/Sample.java")
                .withLines("",
                        "package de.skuzzle;",
                        "//skiped comment",
                        "import java.util.ArrayList;");

        final SourceTreeAnalyzer subject = SourceTreeAnalyzer.getInstance();
        final BannedImportGroup group = BannedImportGroup.builder()
                .withBasePackages("**")
                .withBannedImports("java.util.ArrayList")
                .build();
        final BannedImportGroups groups = BannedImportGroups.builder().withGroup(group).build();
        final AnalyzeResult analyzeResult = subject.analyze(settings, groups);

        final PackagePattern expectedMatchedBy = PackagePattern
                .parse("java.util.ArrayList");
        final AnalyzeResult expected = AnalyzeResult.builder()
                .withMatches(MatchedFile
                        .forSourceFile(sourceFile)
                        .matchedBy(group)
                        .withMatchAt(4, "java.util.ArrayList", expectedMatchedBy))
                .build();

        assertThat(analyzeResult).isEqualTo(expected);
    }

    @Test
    void testFindBannedImportOnCorrectLineWithSkippedBlockComment() throws Exception {
        final Path sourceFile = new SourceFileBuilder(fs)
                .atPath("src/main/java/de/skuzzle/Sample.java")
                .withLines("",
                        "package de.skuzzle;/*",
                        "skiped comment",
                        "*/import java.util.ArrayList;");

        final SourceTreeAnalyzer subject = SourceTreeAnalyzer.getInstance();
        final BannedImportGroup group = BannedImportGroup.builder()
                .withBasePackages("**")
                .withBannedImports("java.util.ArrayList")
                .build();
        final BannedImportGroups groups = BannedImportGroups.builder().withGroup(group).build();
        final AnalyzeResult analyzeResult = subject.analyze(settings, groups);

        final PackagePattern expectedMatchedBy = PackagePattern
                .parse("java.util.ArrayList");
        final AnalyzeResult expected = AnalyzeResult.builder()
                .withMatches(MatchedFile
                        .forSourceFile(sourceFile)
                        .matchedBy(group)
                        .withMatchAt(4, "java.util.ArrayList", expectedMatchedBy))
                .build();

        assertThat(analyzeResult).isEqualTo(expected);
    }

    @Test
    void testLeadingBlockComment() throws Exception {
        final Path sourceFile = new SourceFileBuilder(fs)
                .atPath("src/main/java/de/skuzzle/Sample.java")
                .withLines("",
                        "/*Instrumented*/package de.skuzzle;",
                        "import java.util.ArrayList;");

        final SourceTreeAnalyzer subject = SourceTreeAnalyzer.getInstance();

        final BannedImportGroup group = BannedImportGroup.builder()
                .withBasePackages("**")
                .withBannedImports("java.util.ArrayList")
                .build();
        final BannedImportGroups groups = BannedImportGroups.builder().withGroup(group).build();
        final AnalyzeResult analyzeResult = subject.analyze(settings, groups);

        final PackagePattern expectedMatchedBy = PackagePattern
                .parse("java.util.ArrayList");
        final AnalyzeResult expected = AnalyzeResult.builder()
                .withMatches(MatchedFile
                        .forSourceFile(sourceFile)
                        .matchedBy(group)
                        .withMatchAt(3, "java.util.ArrayList", expectedMatchedBy))
                .build();

        assertThat(analyzeResult).isEqualTo(expected);
    }

    @Test
    void testWeirdComment() throws Exception {
        final Path sourceFile = new SourceFileBuilder(fs)
                .atPath("src/main/java/de/skuzzle/Sample.java")
                .withLines("",
                        "package de.skuzzle.test;",
                        "/** Weird block comment ///**//**/import de.skuzzle.sample.Test5;//de.skuzzle.sample.TestIgnored");

        final SourceTreeAnalyzer subject = SourceTreeAnalyzer.getInstance();

        final BannedImportGroup group = BannedImportGroup.builder()
                .withBasePackages("**")
                .withBannedImports("de.skuzzle.sample.*")
                .build();
        final BannedImportGroups groups = BannedImportGroups.builder().withGroup(group).build();
        final AnalyzeResult analyzeResult = subject.analyze(settings, groups);

        final PackagePattern expectedMatchedBy = PackagePattern
                .parse("de.skuzzle.sample.*");
        final AnalyzeResult expected = AnalyzeResult.builder()
                .withMatches(MatchedFile
                        .forSourceFile(sourceFile)
                        .matchedBy(group)
                        .withMatchAt(3, "de.skuzzle.sample.Test5", expectedMatchedBy))
                .build();

        assertThat(analyzeResult).isEqualTo(expected);
    }

    @Test
    void testDifferentCharset() throws Exception {
        final Path sourceFile = new SourceFileBuilder(fs)
                .atPath("src/main/java/de/skuzzle/Sample.java")
                .witchCharset(StandardCharsets.ISO_8859_1)
                .withLines("",
                        "  package    de.sküzzle;",
                        "  import    jävä.ütil.ArrayList;");

        final AnalyzerSettings localSettings = AnalyzerSettings.builder()
                .withRootDirectories(this.root)
                .withSourceFileCharset(StandardCharsets.ISO_8859_1)
                .build();
        final SourceTreeAnalyzer subject = SourceTreeAnalyzer.getInstance();

        final BannedImportGroup group = BannedImportGroup.builder()
                .withBasePackages("de.sküzzle.**")
                .withBannedImports("jävä.ütil.ArrayList")
                .build();
        final BannedImportGroups groups = BannedImportGroups.builder().withGroup(group).build();
        final AnalyzeResult analyzeResult = subject.analyze(localSettings, groups);

        final PackagePattern expectedMatchedBy = PackagePattern
                .parse("jävä.ütil.ArrayList");
        final AnalyzeResult expected = AnalyzeResult.builder()
                .withMatches(MatchedFile
                        .forSourceFile(sourceFile)
                        .matchedBy(group)
                        .withMatchAt(3, "jävä.ütil.ArrayList", expectedMatchedBy))
                .build();

        assertThat(analyzeResult).isEqualTo(expected);
    }

    @Test
    void testFindBannedImportInMultipleBasePackages() throws Exception {
        final Path sourceFile1 = new SourceFileBuilder(fs)
                .atPath("src/main/java/de/skuzzle1/Sample.java")
                .withLines(
                        "package de.skuzzle1;",
                        "import java.util.ArrayList;");
        final Path sourceFile2 = new SourceFileBuilder(fs)
                .atPath("src/main/java/de/skuzzle2/Sample2.java")
                .withLines(
                        "package de.skuzzle2;",
                        "import java.util.ArrayList;");

        final SourceTreeAnalyzer subject = SourceTreeAnalyzer.getInstance();

        final BannedImportGroup group = BannedImportGroup.builder()
                .withBasePackages("de.skuzzle1.**", "de.skuzzle2.**")
                .withBannedImports("java.util.ArrayList")
                .build();
        final BannedImportGroups groups = BannedImportGroups.builder().withGroup(group).build();
        final AnalyzeResult analyzeResult = subject.analyze(settings, groups);

        final PackagePattern expectedMatchedBy = PackagePattern
                .parse("java.util.ArrayList");
        final AnalyzeResult expected = AnalyzeResult.builder()
                .withMatches(
                        MatchedFile
                                .forSourceFile(sourceFile1)
                                .matchedBy(group)
                                .withMatchAt(2, "java.util.ArrayList", expectedMatchedBy),
                        MatchedFile.forSourceFile(sourceFile2)
                                .matchedBy(group)
                                .withMatchAt(2, "java.util.ArrayList", expectedMatchedBy))
                .build();

        assertThat(analyzeResult).isEqualTo(expected);
    }

    @Test
    void testSkipNonJavaFile() throws Exception {
        new SourceFileBuilder(fs)
                .atPath("src/main/java/de/skuzzle/Sample.NOT_JAVA")
                .withLines("package de.skuzzle;", "import java.util.ArrayList;");

        final SourceTreeAnalyzer subject = SourceTreeAnalyzer.getInstance();

        final BannedImportGroup group = BannedImportGroup.builder()
                .withBasePackages("**")
                .withBannedImports("java.util.ArrayList")
                .build();
        final BannedImportGroups groups = BannedImportGroups.builder().withGroup(group).build();
        final AnalyzeResult analyzeResult = subject.analyze(settings, groups);

        assertThat(analyzeResult.bannedImportsFound()).isFalse();
    }

    @Test
    void testEverythingExcludedByBasePackage() throws Exception {
        new SourceFileBuilder(fs)
                .atPath("src/main/java/de/skuzzle/Sample.java")
                .withLines("",
                        "package de.skuzzle;",
                        "import java.util.ArrayList;");

        final SourceTreeAnalyzer subject = SourceTreeAnalyzer.getInstance();
        final AnalyzeResult analyzeResult = subject.analyze(settings,
                BannedImportGroups.builder().withGroup(BannedImportGroup.builder()
                        .withBasePackages("com.foo.*")
                        .withBannedImports("java.util.ArrayList"))
                        .build());

        assertThat(analyzeResult.bannedImportsFound()).isFalse();
    }

    @Test
    void testEverythingExcludedByExclusion() throws Exception {
        new SourceFileBuilder(fs)
                .atPath("src/main/java/de/skuzzle/Sample.java")
                .withLines("",
                        "package de.skuzzle;",
                        "import java.util.ArrayList;");

        final SourceTreeAnalyzer subject = SourceTreeAnalyzer.getInstance();
        final AnalyzeResult analyzeResult = subject.analyze(settings,
                BannedImportGroups.builder().withGroup(BannedImportGroup.builder()
                        .withBasePackages("de.skuzzle.*")
                        .withBannedImports("java.util.ArrayList")
                        .withExcludedClasses("de.skuzzle.Sample"))
                        .build());

        assertThat(analyzeResult.bannedImportsFound()).isFalse();
    }

    @Test
    void testAllowedImport() throws Exception {
        new SourceFileBuilder(fs)
                .atPath("src/main/java/de/skuzzle/Sample.java")
                .withLines("",
                        "package de.skuzzle;",
                        "import java.util.ArrayList;");

        final SourceTreeAnalyzer subject = SourceTreeAnalyzer.getInstance();
        final AnalyzeResult analyzeResult = subject.analyze(settings,
                BannedImportGroups.builder().withGroup(BannedImportGroup.builder()
                        .withBasePackages("**")
                        .withBannedImports("java.util.*")
                        .withAllowedImports("java.util.ArrayList"))
                        .build());

        assertThat(analyzeResult.bannedImportsFound()).isFalse();
    }

    @Test
    void testMultipleGroupsMatchMostSpecific() throws Exception {
        final Path sourceFile = new SourceFileBuilder(fs)
                .atPath("src/main/java/de/skuzzle/Sample.java")
                .withLines("",
                        "package de.skuzzle;",
                        "import com.sun.WhatEver;",
                        "import java.util.ArrayList;");

        final Builder matchedGroup = BannedImportGroup.builder()
                .withBasePackages("de.skuzzle.**")
                .withBannedImports("com.sun.**");
        final BannedImportGroups groups = BannedImportGroups.builder()
                .withGroup(BannedImportGroup.builder()
                        .withBasePackages("**")
                        .withBannedImports("java.util.*"))
                .withGroup(matchedGroup)
                .build();

        final SourceTreeAnalyzer subject = SourceTreeAnalyzer.getInstance();
        final AnalyzeResult result = subject.analyze(settings, groups);

        final PackagePattern expectedMatchedBy = PackagePattern.parse("com.sun.**");
        final AnalyzeResult expectedResult = AnalyzeResult.builder()
                .withMatches(
                        MatchedFile.forSourceFile(sourceFile)
                                .withMatchAt(3, "com.sun.WhatEver", expectedMatchedBy)
                                .matchedBy(matchedGroup.build()))
                .build();

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testMultipleGroupsMatchMostSpecificWithExclusion() throws Exception {
        new SourceFileBuilder(fs)
                .atPath("src/main/java/de/skuzzle/Sample.java")
                .withLines("",
                        "package de.skuzzle;",
                        "import com.sun.WhatEver;",
                        "import java.util.ArrayList;");

        final BannedImportGroups groups = BannedImportGroups.builder()
                .withGroup(BannedImportGroup.builder()
                        .withBasePackages("**")
                        .withBannedImports("java.util.*"))
                .withGroup(BannedImportGroup.builder()
                        .withBasePackages("de.skuzzle.**")
                        .withBannedImports("com.sun.**")
                        .withExcludedClasses("de.skuzzle.Sample"))
                .build();

        final SourceTreeAnalyzer subject = SourceTreeAnalyzer.getInstance();
        final AnalyzeResult result = subject.analyze(settings, groups);
        assertThat(result.bannedImportsFound()).isFalse();
    }

    @Test
    void testMultipleGroupsNoMatchInMostSpecific() throws Exception {
        new SourceFileBuilder(fs)
                .atPath("src/main/java/de/skuzzle/Sample.java")
                .withLines("",
                        "package de.skuzzle;",
                        "import com.sun.WhatEver;",
                        "import java.util.ArrayList;");

        final Builder matchedGroup = BannedImportGroup.builder()
                .withBasePackages("de.skuzzle.**")
                .withBannedImports("does.not.Occur");
        final BannedImportGroups groups = BannedImportGroups.builder()
                .withGroup(BannedImportGroup.builder()
                        .withBasePackages("**")
                        .withBannedImports("java.util.*"))
                .withGroup(matchedGroup)
                .build();

        final SourceTreeAnalyzer subject = SourceTreeAnalyzer.getInstance();
        final AnalyzeResult result = subject.analyze(settings, groups);

        assertThat(result.bannedImportsFound()).isFalse();
    }
}
