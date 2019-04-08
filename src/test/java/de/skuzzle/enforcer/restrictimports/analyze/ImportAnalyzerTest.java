package de.skuzzle.enforcer.restrictimports.analyze;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.skuzzle.enforcer.restrictimports.parser.ImportStatement;
import de.skuzzle.enforcer.restrictimports.parser.ParsedFile;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

class ImportAnalyzerTest {

    private final ImportAnalyzer subject = new ImportAnalyzer();

    private final ParsedFile parsedFile = parsedFile("File", "de.skuzzle.test",
            "de.skuzzle.sample.Test",
            "foo.bar.xyz",
            "de.skuzzle.sample.Test2",
            "de.skuzzle.sample.Test3",
            "de.foo.bar.Test");

    private ParsedFile parsedFile(String className, String packageName, String... lines) {
        final String fqcn = packageName + "." + className;
        final Path path = mock(Path.class);
        final Path fileName = mock(Path.class);
        when(path.getFileName()).thenReturn(fileName);
        when(fileName.toString()).thenReturn(className + ".java");
        final List<ImportStatement> imports = new ArrayList<>();

        for (int lineNumber = 0; lineNumber < lines.length; ++lineNumber) {
            imports.add(new ImportStatement(lines[lineNumber], lineNumber + 1));
        }
        return new ParsedFile(path, packageName, fqcn, imports);

    }

    @Test
    void testMatchBannedOnly() throws Exception {
        final BannedImportGroups groups = BannedImportGroups.builder()
                .withGroup(BannedImportGroup.builder()
                        .withBasePackages("foo.bar", "de.skuzzle.test.*")
                        .withBannedImports("de.skuzzle.sample.*"))
                .build();
           final Optional<MatchedFile> matches = this.subject.matchFile(parsedFile, groups);

        final PackagePattern expectedMatchedBy = PackagePattern
                .parse("de.skuzzle.sample.*");
        final ImmutableList<MatchedImport> expected = ImmutableList.of(
                new MatchedImport(1, "de.skuzzle.sample.Test", expectedMatchedBy),
                new MatchedImport(3, "de.skuzzle.sample.Test2", expectedMatchedBy),
                new MatchedImport(4, "de.skuzzle.sample.Test3", expectedMatchedBy));

        assertThat(matches.get().getMatchedImports()).isEqualTo(expected);
    }

    @Test
    void testMatchWithInclude() throws Exception {
        final BannedImportGroups groups = BannedImportGroups.builder()
                .withGroup(BannedImportGroup.builder()
                        .withBasePackages("**")
                        .withBannedImports("de.skuzzle.sample.*")
                        .withAllowedImports("de.skuzzle.sample.Test2", "de.skuzzle.sample.Test4"))
                .build();
        final Optional<MatchedFile> matches = this.subject.matchFile(this.parsedFile, groups);

        final PackagePattern expectedMatchedBy = PackagePattern
                .parse("de.skuzzle.sample.*");
        final ImmutableList<MatchedImport> expected = ImmutableList.of(
                new MatchedImport(1, "de.skuzzle.sample.Test", expectedMatchedBy),
                new MatchedImport(4, "de.skuzzle.sample.Test3", expectedMatchedBy));

        assertThat(matches.get().getMatchedImports()).isEqualTo(expected);
    }

    @Test
    void testExcludeFile() throws Exception {
        final BannedImportGroups groups = BannedImportGroups.builder()
                .withGroup(BannedImportGroup.builder()
                        .withBasePackages("**")
                        .withBannedImports("foo")
                        .withExcludedClasses("de.skuzzle.test.File")
                        .withReason("message"))
                .build();

        final Optional<MatchedFile> matches = this.subject.matchFile(this.parsedFile, groups);
        assertThat(matches).isEmpty();
    }

    @Test
    void testLeadingEmptyLinesDefaultPackages() throws Exception {
        final ParsedFile parsedFile = parsedFile("File", "",
                "de.skuzzle.sample.Test");

        final BannedImportGroups groups = BannedImportGroups.builder()
                .withGroup(BannedImportGroup.builder()
                        .withBasePackages("**")
                        .withBannedImports("de.skuzzle.sample.**"))
                .build();

        assertThat(subject.matchFile(parsedFile, groups).get().getMatchedImports()).first()
                .isEqualTo(new MatchedImport(1,
                        "de.skuzzle.sample.Test", PackagePattern.parse("de.skuzzle.sample.**")));
    }

    @Test
    void testExcludeWholeFileByBasePackage() throws Exception {
        final Optional<MatchedFile> matches = this.subject.matchFile(this.parsedFile,
                BannedImportGroups.builder()
                        .withGroup(BannedImportGroup.builder()
                                .withBasePackages("de.foo.bar")
                                .withBannedImports("de.skuzzle.sample.*"))
                        .build());
        assertThat(matches).isEmpty();
    }
}
