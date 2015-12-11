package de.skuzzle.enforcer.restrictimports;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;

import de.skuzzle.enforcer.restrictimports.ImportMatcherImpl.LineSupplier;

@RunWith(MockitoJUnitRunner.class)
public class ImportMatcherImplTest {

    @Mock
    private LineSupplier mockLineSupplier;
    @InjectMocks
    private ImportMatcherImpl subject;

    private final Path path = mock(Path.class);

    @Before
    public void setUp() throws Exception {
        when(this.path.toString()).thenReturn("path/to/file");
        when(this.mockLineSupplier.lines(this.path)).thenReturn(ImmutableList.of(
                "package de.skuzzle.test;",
                "",
                "import de.skuzzle.sample.Test;",
                "import   foo.bar.xyz;",
                "import de.skuzzle.sample.Test2;",
                "import de.foo.bar.Test").stream());
    }

    @Test(expected = RuntimeIOException.class)
    public void testException() throws Exception {
        when(this.mockLineSupplier.lines(this.path)).thenThrow(new IOException());
        this.subject.matchFile(this.path,
                ImmutableList.of(),
                ImmutableList.of()).collect(Collectors.toList());
    }

    @Test
    public void testMatchBannedOnly() throws Exception {
        final List<PackagePattern> banned = ImmutableList.of(
                PackagePattern.parse("de.skuzzle.sample.*"));

        final List<Match> matches = this.subject.matchFile(this.path, banned,
                ImmutableList.of()).collect(Collectors.toList());

        assertEquals(2, matches.size());
        final Match match1 = matches.get(0);
        assertEquals("de.skuzzle.sample.Test", match1.getMatchedString());
        assertEquals(3, match1.getImportLine());
        assertEquals("path/to/file", match1.getSourceFile());

        final Match match2 = matches.get(1);
        assertEquals(5, match2.getImportLine());
        assertEquals("de.skuzzle.sample.Test2", match2.getMatchedString());
        assertEquals("path/to/file", match2.getSourceFile());
    }

    @Test
    public void testMatchWithInclude() throws Exception {
        final List<PackagePattern> banned = ImmutableList.of(PackagePattern.parse("de.skuzzle.sample.*"));
        final List<PackagePattern> include = ImmutableList.of(PackagePattern.parse("de.skuzzle.sample.Test2"));
        final List<Match> matches = this.subject.matchFile(this.path, banned, include)
                .collect(Collectors.toList());

        assertEquals(1, matches.size());
        final Match match1 = matches.get(0);
        assertEquals("de.skuzzle.sample.Test", match1.getMatchedString());
        assertEquals(3, match1.getImportLine());
        assertEquals("path/to/file", match1.getSourceFile());
    }
}
