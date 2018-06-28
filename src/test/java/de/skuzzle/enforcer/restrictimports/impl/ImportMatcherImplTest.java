package de.skuzzle.enforcer.restrictimports.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;

import de.skuzzle.enforcer.restrictimports.api.RuntimeIOException;
import de.skuzzle.enforcer.restrictimports.impl.ImportMatcherImpl.LineSupplier;
import de.skuzzle.enforcer.restrictimports.model.BannedImportGroup;
import de.skuzzle.enforcer.restrictimports.model.Match;
import de.skuzzle.enforcer.restrictimports.model.PackagePattern;

@RunWith(MockitoJUnitRunner.class)
public class ImportMatcherImplTest {

    @Mock
    private LineSupplier mockLineSupplier;
    @InjectMocks
    private ImportMatcherImpl subject;

    private final Path path = mock(Path.class);
    private final Path fileName = mock(Path.class);

    @Before
    public void setUp() throws Exception {
        when(this.path.getFileName()).thenReturn(this.fileName);
        when(this.fileName.toString()).thenReturn("File.java");
        when(this.path.toString()).thenReturn("path/to/File.java");
        when(this.mockLineSupplier.lines(this.path)).thenReturn(ImmutableList.of(
                "package de.skuzzle.test;",
                "",
                "import de.skuzzle.sample.Test;",
                "import   foo.bar.xyz;",
                "import de.skuzzle.sample.Test2;",
                "import de.foo.bar.Test").stream());
    }

    private BannedImportGroup group(List<String> basePackages, List<String> banned) {
        return group(basePackages, banned, ImmutableList.of());
    }

    private BannedImportGroup group(List<String> basePackages, List<String> banned,
            List<String> allowed) {
        return new BannedImportGroup(
                PackagePattern.parseAll(basePackages),
                PackagePattern.parseAll(banned),
                PackagePattern.parseAll(allowed),
                ImmutableList.of(),
                "message");
    }

    @Test(expected = RuntimeIOException.class)
    public void testException() throws Exception {
        when(this.mockLineSupplier.lines(this.path)).thenThrow(new IOException());
        this.subject.matchFile(this.path,
                group(Arrays.asList("**"), ImmutableList.of("bla")))
                .collect(Collectors.toList());
    }

    @Test
    public void testMatchBannedOnly() throws Exception {
        final List<String> banned = ImmutableList.of("de.skuzzle.sample.*");

        final List<Match> matches = this.subject.matchFile(this.path,
                group(Arrays.asList("foo.bar", "de.skuzzle.test.*"),
                        banned, ImmutableList.of()))
                .collect(Collectors.toList());

        assertEquals(2, matches.size());
        final Match match1 = matches.get(0);
        assertEquals("de.skuzzle.sample.Test", match1.getMatchedString());
        assertEquals(3, match1.getImportLine());
        assertEquals("path/to/File.java", match1.getSourceFile());

        final Match match2 = matches.get(1);
        assertEquals(5, match2.getImportLine());
        assertEquals("de.skuzzle.sample.Test2", match2.getMatchedString());
        assertEquals("path/to/File.java", match2.getSourceFile());
    }

    @Test
    public void testMatchWithInclude() throws Exception {
        final List<String> banned = ImmutableList.of("de.skuzzle.sample.*");
        final List<String> include = ImmutableList.of("de.skuzzle.sample.Test2");
        final List<Match> matches = this.subject
                .matchFile(this.path, group(Arrays.asList("**"), banned, include))
                .collect(Collectors.toList());

        assertEquals(1, matches.size());
        final Match match1 = matches.get(0);
        assertEquals("de.skuzzle.sample.Test", match1.getMatchedString());
        assertEquals(3, match1.getImportLine());
        assertEquals("path/to/File.java", match1.getSourceFile());
    }

    @Test
    public void testExcludeFile() throws Exception {
        final BannedImportGroup group = new BannedImportGroup(
                Arrays.asList(PackagePattern.parse("**")),
                PackagePattern.parseAll(ImmutableList.of("foo")),
                ImmutableList.of(),
                PackagePattern.parseAll(ImmutableList.of("de.skuzzle.test.File")),
                "message");

        final Stream<Match> matches = this.subject.matchFile(this.path, group);

        assertFalse(matches.iterator().hasNext());
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

        final BannedImportGroup group = new BannedImportGroup(
                Arrays.asList(PackagePattern.parse("**")),
                PackagePattern.parseAll(ImmutableList.of("foo")),
                ImmutableList.of(),
                PackagePattern.parseAll(ImmutableList.of("de.skuzzle.test.File")),
                "message");

        final Stream<Match> matches = this.subject.matchFile(this.path, group);

        assertFalse(matches.iterator().hasNext());
    }

    @Test
    public void testExcludeWholeFile() throws Exception {
        final Stream<Match> matches = this.subject.matchFile(this.path,
                group(Arrays.asList("de.foo.bar"),
                        ImmutableList.of("de.skuzzle.sample.*")));

        assertFalse(matches.iterator().hasNext());
    }
}
