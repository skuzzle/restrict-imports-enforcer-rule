package de.skuzzle.enforcer.restrictimports;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Properties;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExceptionFormattingTest {

    private final EnforcerRuleHelper helper = mock(EnforcerRuleHelper.class);
    private final Log log = mock(Log.class);
    private final MavenProject mavenProject = mock(MavenProject.class);

    private final RestrictImports subject = new RestrictImports();

    @BeforeEach
    public void setUp() throws Exception {
        when(this.helper.getLog()).thenReturn(this.log);
        when(this.helper.evaluate("${project}")).thenReturn(this.mavenProject);

        final URL url = getClass().getResource("/SampleJavaFile.java");
        final File f = new File(url.toURI());
        final Path path = f.toPath().getParent();
        when(this.mavenProject.getProperties()).thenReturn(new Properties());
        when(this.mavenProject.getCompileSourceRoots())
                .thenReturn(Collections.singletonList(path.toString()));
    }

    @Test
    void testFormatWithReason() throws Exception {
        this.subject.setBannedImports(Collections.singletonList("java.util.**"));
        this.subject.setReason("Some reason");

        assertThatExceptionOfType(EnforcerRuleException.class)
                .isThrownBy(() -> this.subject.execute(helper))
                .withMessage("\nBanned imports detected:\n" +
                        "Reason: Some reason\n" +
                        "\tin file: SampleJavaFile.java\n" +
                        "\t\tjava.util.ArrayList (Line: 3, Matched by: java.util.**)\n");
    }
}
