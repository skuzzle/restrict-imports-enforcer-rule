package de.skuzzle.enforcer.restrictimports.analyze;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

import de.skuzzle.enforcer.restrictimports.analyze.ImportMatcherImpl.LineSupplier;

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
                "import de.skuzzle.sample.Test3;//inline comment",
                "/*block comment */import de.skuzzle.sample.Test4;",
                "/** Weird block comment ///**//**/import de.skuzzle.sample.Test5;//de.skuzzle.sample.TestIgnored",
                "import de.foo.bar.Test").stream());
    }

    @Test
    public void testException() throws Exception {
        // when(this.mockLineSupplier.lines(this.path)).thenThrow(new IOException());

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

        final ImmutableList<MatchedImport> expected = ImmutableList.of(
                new MatchedImport(path, 3, "de.skuzzle.sample.Test"),
                new MatchedImport(path, 5, "de.skuzzle.sample.Test2"),
                new MatchedImport(path, 6, "de.skuzzle.sample.Test3"),
                new MatchedImport(path, 7, "de.skuzzle.sample.Test4"),
                new MatchedImport(path, 8, "de.skuzzle.sample.Test5"));

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

        final ImmutableList<MatchedImport> expected = ImmutableList.of(
                new MatchedImport(path, 3, "de.skuzzle.sample.Test"),
                new MatchedImport(path, 6, "de.skuzzle.sample.Test3"),
                new MatchedImport(path, 8, "de.skuzzle.sample.Test5"));

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
    public void testExcludeInstrumentedFile() throws Exception {
        when(this.mockLineSupplier.lines(this.path)).thenReturn(ImmutableList.of(
                "/*Instrumented*/package de.skuzzle.test;",
                "",
                "import de.skuzzle.sample.Test;",
                "import   foo.bar.xyz;",
                "import de.skuzzle.sample.Test2;",
                "import de.foo.bar.Test").stream());

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
    public void testExcludeWholeFileByBasePackage() throws Exception {
        final Stream<MatchedImport> matches = this.subject.matchFile(this.path,
                BannedImportGroup.builder()
                        .withBasePackages("de.foo.bar")
                        .withBannedImports("de.skuzzle.sample.*")
                        .build());
        assertThat(matches).isEmpty();
    }
}
