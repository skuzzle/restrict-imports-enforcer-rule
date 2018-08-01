package de.skuzzle.enforcer.restrictimports.analyze;

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

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;

import de.skuzzle.enforcer.restrictimports.analyze.ImportMatcherImpl.LineSupplier;

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
                "import de.skuzzle.sample.Test3;//inline comment",
                "/*block comment */import de.skuzzle.sample.Test4;",
                "/** Weird block comment ///**//**/import de.skuzzle.sample.Test5;//de.skuzzle.sample.TestIgnored",
                "import de.foo.bar.Test").stream());
    }

    private BannedImportGroup group(List<String> basePackages, List<String> banned)
            throws EnforcerRuleException {
        return group(basePackages, banned, ImmutableList.of());
    }

    private BannedImportGroup group(List<String> basePackages, List<String> banned,
            List<String> allowed) throws EnforcerRuleException {
        return BannedImportGroup.builder()
                .withBasePackages(PackagePattern.parseAll(basePackages))
                .withBannedImports(PackagePattern.parseAll(banned))
                .withAllowedImports(PackagePattern.parseAll(allowed))
                .withExcludedClasses(ImmutableList.of())
                .withReason("message")
                .build();
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

        assertEquals(5, matches.size());
        final Match match1 = matches.get(0);
        assertEquals("de.skuzzle.sample.Test", match1.getMatchedString());
        assertEquals(3, match1.getImportLine());
        assertEquals("path/to/File.java", match1.getSourceFile().toString());

        final Match match2 = matches.get(1);
        assertEquals(5, match2.getImportLine());
        assertEquals("de.skuzzle.sample.Test2", match2.getMatchedString());
        assertEquals("path/to/File.java", match2.getSourceFile().toString());

        final Match match3 = matches.get(2);
        assertEquals(6, match3.getImportLine());
        assertEquals("de.skuzzle.sample.Test3", match3.getMatchedString());
        assertEquals("path/to/File.java", match3.getSourceFile().toString());

        final Match match4 = matches.get(3);
        assertEquals(7, match4.getImportLine());
        assertEquals("de.skuzzle.sample.Test4", match4.getMatchedString());
        assertEquals("path/to/File.java", match4.getSourceFile().toString());

        final Match match5 = matches.get(4);
        assertEquals(8, match5.getImportLine());
        assertEquals("de.skuzzle.sample.Test5", match5.getMatchedString());
        assertEquals("path/to/File.java", match5.getSourceFile().toString());
    }

    @Test
    public void testMatchBannedStaticImports() throws Exception {
        when(this.mockLineSupplier.lines(this.path)).thenReturn(ImmutableList.of(
                "package de.skuzzle.test;",
                "",
                "import static de.skuzzle.sample1.Test.CONSTANT;",
                "import static de.skuzzle.sample2.Test.CONSTANT;").stream());

        final List<String> banned = Arrays.asList("de.skuzzle.sample1.**", "de.skuzzle.sample2.*");

        final List<Match> matches = this.subject.matchFile(this.path,
                group(Arrays.asList("foo.bar", "de.skuzzle.test.*"),
                        banned, ImmutableList.of()))
                .collect(Collectors.toList());

        assertEquals(1, matches.size());
        final Match match1 = matches.get(0);
        assertEquals("de.skuzzle.sample1.Test.CONSTANT", match1.getMatchedString());
        assertEquals(3, match1.getImportLine());
        assertEquals("path/to/File.java", match1.getSourceFile());
    }

    @Test
    public void testMatchWithInclude() throws Exception {
        final List<String> banned = ImmutableList.of("de.skuzzle.sample.*");
        final List<String> include = ImmutableList.of(
                "de.skuzzle.sample.Test2",
                "de.skuzzle.sample.Test4");
        final List<Match> matches = this.subject
                .matchFile(this.path, group(Arrays.asList("**"), banned, include))
                .collect(Collectors.toList());

        assertEquals(3, matches.size());
        final Match match1 = matches.get(0);
        assertEquals("de.skuzzle.sample.Test", match1.getMatchedString());
        assertEquals(3, match1.getImportLine());
        assertEquals("path/to/File.java", match1.getSourceFile().toString());

        final Match match2 = matches.get(1);
        assertEquals(6, match2.getImportLine());
        assertEquals("de.skuzzle.sample.Test3", match2.getMatchedString());
        assertEquals("path/to/File.java", match2.getSourceFile().toString());

        final Match match3 = matches.get(2);
        assertEquals(8, match3.getImportLine());
        assertEquals("de.skuzzle.sample.Test5", match3.getMatchedString());
        assertEquals("path/to/File.java", match3.getSourceFile().toString());
    }

    @Test
    public void testExcludeFile() throws Exception {
        final BannedImportGroup group = BannedImportGroup.builder()
                .withBasePackages(Arrays.asList(PackagePattern.parse("**")))
                .withBannedImports(PackagePattern.parseAll(ImmutableList.of("foo")))
                .withAllowedImports(ImmutableList.of())
                .withExcludedClasses(
                        PackagePattern.parseAll(ImmutableList.of("de.skuzzle.test.File")))
                .withReason("message")
                .build();

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

        final BannedImportGroup group = BannedImportGroup.builder()
                .withBasePackages(Arrays.asList(PackagePattern.parse("**")))
                .withBannedImports(PackagePattern.parseAll(ImmutableList.of("foo")))
                .withAllowedImports(ImmutableList.of())
                .withExcludedClasses(
                        PackagePattern.parseAll(ImmutableList.of("de.skuzzle.test.File")))
                .withReason("message")
                .build();

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
