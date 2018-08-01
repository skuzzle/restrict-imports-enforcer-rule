package de.skuzzle.enforcer.restrictimports;

import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MatchFormatterImplTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private EnforcerRuleHelper helper;
    @Mock
    private Log log;
    @Mock
    private MavenProject mavenProject;

    @InjectMocks
    private RestrictImports subject;

    @Before
    public void setUp() throws Exception {
        when(this.helper.getLog()).thenReturn(this.log);
        when(this.helper.evaluate("${project}")).thenReturn(this.mavenProject);

        final URL url = getClass().getResource("/SampleJavaFile.java");
        final File f = new File(url.toURI());
        final Path path = f.toPath().getParent();
        when(this.mavenProject.getCompileSourceRoots())
                .thenReturn(Collections.singletonList(path.toString()));
    }

    @Test
    public void testFormatWithReason() throws Exception {
        this.subject.setBannedImports(Collections.singletonList("java.util.**"));
        this.subject.setReason("Some reason");
        this.exception.expect(EnforcerRuleException.class);
        this.exception.expectMessage("\nBanned imports detected:\n" +
                "Reason: Some reason\n" +
                "\tin file: SampleJavaFile.java\n" +
                "\t\tjava.util.ArrayList (Line: 3)\n");

        this.subject.execute(this.helper);
    }
}
