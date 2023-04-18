package de.skuzzle.enforcer.restrictimports.analyze;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import de.skuzzle.enforcer.restrictimports.parser.Annotation;
import org.junit.jupiter.api.Test;

import de.skuzzle.enforcer.restrictimports.parser.ImportStatement;
import de.skuzzle.enforcer.restrictimports.parser.ImportType;
import de.skuzzle.enforcer.restrictimports.parser.ParsedFile;

class ImportAnalyzerTest {

    private final ImportAnalyzer subject = new ImportAnalyzer();

    private final ParsedFile parsedFile = parsedFile("File", "de.skuzzle.test",
            "de.skuzzle.sample.Test",
            "foo.bar.xyz",
            "de.skuzzle.sample.Test2",
            "de.skuzzle.sample.Test3",
            "de.foo.bar.Test");

    private final ParsedFile parsedFileWithAnnotation = parsedFileWithAnnotation("File", "de.skuzzle.test",
        Annotation.withMessage("Sample Annotation"),
        "de.skuzzle.sample.Test",
        "foo.bar.xyz",
        "de.skuzzle.sample.Test2",
        "de.skuzzle.sample.Test3",
        "de.foo.bar.Test");

    private ParsedFile parsedFile(String className, String packageName, String... lines) {
        return parsedFileWithAnnotation(className, packageName, null, lines);
    }

    private ParsedFile failedToParse(String fileName, Annotation annotation) {
        final Path path = mock(Path.class);
        final Path pathFileName = mock(Path.class);
        when(path.getFileName()).thenReturn(pathFileName);
        when(pathFileName.toString()).thenReturn(fileName + ".java");
        return ParsedFile.failedToParse(path, annotation);
    }

    private ParsedFile parsedFileWithAnnotation(String className, String packageName, Annotation annotation, String... lines) {
        final String fqcn = packageName + "." + className;
        final Path path = mock(Path.class);
        final Path pathFileName = mock(Path.class);
        when(path.getFileName()).thenReturn(pathFileName);
        when(pathFileName.toString()).thenReturn(className + ".java");
        final List<ImportStatement> imports = new ArrayList<>();

        for (int lineNumber = 0; lineNumber < lines.length; ++lineNumber) {
            imports.add(new ImportStatement(lines[lineNumber], lineNumber + 1, ImportType.IMPORT));
        }
        final ParsedFile result = ParsedFile.successful(path, packageName, fqcn, imports);
        if (annotation != null) {
            return result.andAddAnnotation(annotation);
        }
        return result;
    }

    @Test
    void testMatchFailedToParse() {
        final BannedImportGroups groups = BannedImportGroups.builder()
            .withGroup(BannedImportGroup.builder()
                .withBasePackages("**")
                .withBannedImports("not.in.that.file.**")
                .build())
            .build();

        ParsedFile failedToParseFile = failedToParse("Filename", Annotation.withMessage("Error while parsing"));
        final Optional<MatchedFile> matches = this.subject.matchFile(failedToParseFile, groups);
        final MatchedFile expectedMatchedFile = MatchedFile.forSourceFile(failedToParseFile.getPath())
            .withWarnings(Warning.withMessage("Error while parsing"))
            .withFailedToParse(true).build();

        assertThat(matches).isEqualTo(Optional.of(expectedMatchedFile));
    }

    @Test
    void testNoMatchedImportsButWithAnnotations() {
        final BannedImportGroups groups = BannedImportGroups.builder()
            .withGroup(BannedImportGroup.builder()
                .withBasePackages("does.not.match.the.sample.file.**")
                .withBannedImports("**")
                .build())
            .build();

        final Optional<MatchedFile> matches = this.subject.matchFile(parsedFileWithAnnotation, groups);

        assertThat(matches).isPresent();
        assertThat(matches.get().getMatchedBy()).isEmpty();
        assertThat(matches.get().getMatchedImports()).isEmpty();
        assertThat(matches.get().getWarnings()).containsExactly(Warning.withMessage("Sample Annotation"));
    }

    @Test
    void testWithMatchesAndAnnotations() {
        final BannedImportGroups groups = BannedImportGroups.builder()
            .withGroup(BannedImportGroup.builder()
                .withBasePackages("**")
                .withBannedImports("foo.**")
                .build())
            .build();

        final Optional<MatchedFile> matches = this.subject.matchFile(parsedFileWithAnnotation, groups);

        assertThat(matches).isPresent();
        assertThat(matches.get().getMatchedBy()).isPresent();
        assertThat(matches.get().getMatchedImports()).containsExactly(
            new MatchedImport(2, "foo.bar.xyz", PackagePattern.parse("foo.**")));
        assertThat(matches.get().getWarnings()).containsExactly(Warning.withMessage("Sample Annotation"));
    }

    @Test
    void testMatchBannedOnly() {
        final BannedImportGroups groups = BannedImportGroups.builder()
                .withGroup(BannedImportGroup.builder()
                        .withBasePackages("foo.bar", "de.skuzzle.test.*")
                        .withBannedImports("de.skuzzle.sample.*"))
                .build();
        final Optional<MatchedFile> matches = this.subject.matchFile(parsedFile, groups);

        final PackagePattern expectedMatchedBy = PackagePattern
                .parse("de.skuzzle.sample.*");
        final List<MatchedImport> expected = Arrays.asList(
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
        final List<MatchedImport> expected = Arrays.asList(
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
                        .withExclusions("de.skuzzle.test.File")
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
