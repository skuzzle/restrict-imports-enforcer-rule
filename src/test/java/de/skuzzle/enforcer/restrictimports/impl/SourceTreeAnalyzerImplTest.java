package de.skuzzle.enforcer.restrictimports.impl;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;

import de.skuzzle.enforcer.restrictimports.BannedImportGroup;
import de.skuzzle.enforcer.restrictimports.IOUtils;
import de.skuzzle.enforcer.restrictimports.ImportMatcher;
import de.skuzzle.enforcer.restrictimports.Match;

@RunWith(MockitoJUnitRunner.class)
public class SourceTreeAnalyzerImplTest {

    @Mock
    private ImportMatcher matcher;
    @Mock
    private IOUtils ioUtil;
    @InjectMocks
    private SourceTreeAnalyzerImpl subject;

    private Path javaFile1;
    private Path javaFile2;

    private Path root;
    private Stream<Path> rootStream;

    @Before
    public void setUp() throws Exception {
        this.root = mock(Path.class);
        this.rootStream = Stream.of(this.root);

        this.javaFile1 = mockFile("Foo.java");
        this.javaFile2 = mockFile("Bar.java");

        when(this.ioUtil.listFiles(eq(this.root), any())).thenReturn(Stream.of(
                this.javaFile1, this.javaFile2));
    }

    @Test
    public void testName() throws Exception {
        final BannedImportGroup group = new BannedImportGroup();
        final Match file1Match = new Match("xyz", 1, "dfdg");
        when(this.matcher.matchFile(this.javaFile1, group)).thenReturn(
                Stream.of(file1Match));
        when(this.matcher.matchFile(this.javaFile2, group)).thenReturn(
                Stream.empty());
        final Map<String, List<Match>> result = this.subject.analyze(
                this.rootStream, ImmutableList.of(group));

        final Match actual = result.get("xyz").get(0);
        assertSame(file1Match, actual);
    }

    private Path mockFile(String fileName) {
        final Path path = mock(Path.class);
        final Path fn = mock(Path.class);
        when(path.getFileName()).thenReturn(fn);
        when(fn.toString()).thenReturn(fileName);
        when(this.ioUtil.isFile(path)).thenReturn(true);
        return path;
    }
}
