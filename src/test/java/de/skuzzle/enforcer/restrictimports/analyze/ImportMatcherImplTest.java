package de.skuzzle.enforcer.restrictimports.analyze;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

public class ImportMatcherImplTest {

    private final LineSupplier mockLineSupplier = mock(LineSupplier.class);
    private final ImportMatcherImpl subject = new ImportMatcherImpl(mockLineSupplier);

    private final Path path = mock(Path.class);
    private final Path fileName = mock(Path.class);

    @BeforeEach
    public void setUp() throws Exception {
        when(this.path.getFileName()).thenReturn(this.fileName);
        when(this.fileName.toString()).thenReturn("File.java");
        when(this.mockLineSupplier.lines(this.path)).thenReturn(ImmutableList.of(
                "package de.skuzzle.test;",
                "",
                "import de.skuzzle.sample.Test;",
                "import   foo.bar.xyz;",
                "import de.skuzzle.sample.Test2;",
                "import de.skuzzle.sample.Test3;",
                "import de.foo.bar.Test").stream());
    }

    @Test
    public void testException() throws Exception {
        when(this.mockLineSupplier.lines(this.path)).thenThrow(new IOException());

        assertThatExceptionOfType(RuntimeIOException.class)
                .isThrownBy(() -> this.subject
                        .matchFile(this.path, BannedImportGroup.builder()
                                .withBasePackages("**")
                                .withBannedImports("bla")
                                .build())
                        .collect(Collectors.toList()));
    }

    @Test
    public void testMatchBannedOnly() throws Exception {
        final List<MatchedImport> matches = this.subject
                .matchFile(this.path, BannedImportGroup.builder()
                        .withBasePackages("foo.bar", "de.skuzzle.test.*")
                        .withBannedImports("de.skuzzle.sample.*")
                        .build())
                .collect(Collectors.toList());

        final PackagePattern expectedMatchedBy = PackagePattern
                .parse("de.skuzzle.sample.*");
        final ImmutableList<MatchedImport> expected = ImmutableList.of(
                new MatchedImport(3, "de.skuzzle.sample.Test", expectedMatchedBy),
                new MatchedImport(5, "de.skuzzle.sample.Test2", expectedMatchedBy),
                new MatchedImport(6, "de.skuzzle.sample.Test3", expectedMatchedBy));

        assertThat(matches).isEqualTo(expected);
    }

    @Test
    public void testMatchWithInclude() throws Exception {
        final List<MatchedImport> matches = this.subject
                .matchFile(this.path, BannedImportGroup.builder()
                        .withBasePackages("**")
                        .withBannedImports("de.skuzzle.sample.*")
                        .withAllowedImports("de.skuzzle.sample.Test2",
                                "de.skuzzle.sample.Test4")
                        .build())
                .collect(Collectors.toList());

        final PackagePattern expectedMatchedBy = PackagePattern
                .parse("de.skuzzle.sample.*");
        final ImmutableList<MatchedImport> expected = ImmutableList.of(
                new MatchedImport(3, "de.skuzzle.sample.Test", expectedMatchedBy),
                new MatchedImport(6, "de.skuzzle.sample.Test3", expectedMatchedBy));

        assertThat(matches).isEqualTo(expected);
    }

    @Test
    public void testExcludeFile() throws Exception {
        final BannedImportGroup group = BannedImportGroup.builder()
                .withBasePackages("**")
                .withBannedImports("foo")
                .withExcludedClasses("de.skuzzle.test.File")
                .withReason("message")
                .build();

        final Stream<MatchedImport> matches = this.subject.matchFile(this.path, group);
        assertThat(matches).isEmpty();
    }

    @Test
    public void testLeadingEmptyLines() throws Exception {
        when(this.mockLineSupplier.lines(this.path)).thenReturn(ImmutableList.of(
                "",
                "",
                "package de.skuzzle.test;",
                "",
                "import de.skuzzle.sample.Test;").stream());

        final BannedImportGroup group = BannedImportGroup.builder()
                .withBasePackages("de.skuzzle.test.**")
                .withBannedImports("de.skuzzle.sample.**")
                .build();
        assertThat(subject.matchFile(path, group)).first().isEqualTo(new MatchedImport(5,
                "de.skuzzle.sample.Test", PackagePattern.parse("de.skuzzle.sample.**")));
    }

    @Test
    public void testLeadingEmptyLinesDefaultPackages() throws Exception {
        when(this.mockLineSupplier.lines(this.path)).thenReturn(ImmutableList.of(
                "",
                "",
                "import de.skuzzle.sample.Test;").stream());

        final BannedImportGroup group = BannedImportGroup.builder()
                .withBasePackages("**")
                .withBannedImports("de.skuzzle.sample.**")
                .build();

        assertThat(subject.matchFile(path, group)).first().isEqualTo(new MatchedImport(3,
                "de.skuzzle.sample.Test", PackagePattern.parse("de.skuzzle.sample.**")));
    }

    @Test
    public void testExcludeWholeFileByBasePackage() throws Exception {
        final Stream<MatchedImport> matches = this.subject.matchFile(this.path,
                BannedImportGroup.builder()
                        .withBasePackages("de.foo.bar")
                        .withBannedImports("de.skuzzle.sample.*")
                        .build());
        assertThat(matches).isEmpty();
    }
}
