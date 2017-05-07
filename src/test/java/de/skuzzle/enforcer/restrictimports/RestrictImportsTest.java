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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RestrictImportsTest {

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
    public void testRestrictImportsNoFailure() throws Exception {
        this.subject.setBannedImport("foo.com.**");
        this.subject.execute(this.helper);
    }

    @Test(expected = EnforcerRuleException.class)
    public void testRestrictFailure() throws Exception {
        this.subject.setBannedImports(Collections.singletonList("java.util.**"));
        this.subject.execute(this.helper);
    }

    @Test
    public void testExcludedByBasePackage() throws Exception {
        this.subject.setBasePackage("foo.bar");
        this.subject.setBannedImports(Collections.singletonList("java.util.**"));
        this.subject.execute(this.helper);
    }

    @Test
    public void testExcludedClass() throws Exception {
        this.subject.setBannedImports(Collections.singletonList("java.util.**"));
        this.subject.setExclusions(Collections.singletonList("de.skuzzle.**"));
        this.subject.execute(this.helper);
    }

    @Test
    public void testAllowImport() throws Exception {
        this.subject.setBannedImports(Collections.singletonList("java.util.**"));
        this.subject.setAllowedImports(Collections.singletonList("java.util.ArrayList"));
        this.subject.execute(this.helper);
    }
}
