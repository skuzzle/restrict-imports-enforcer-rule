package de.skuzzle.enforcer.restrictimports.rule;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RestrictImportsTest {

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
    void testRestrictImportsNoFailure() throws Exception {
        this.subject.setBannedImport("foo.com.**");
        this.subject.execute(this.helper);
    }

    @Test
    void testRestrictFailure() throws Exception {
        this.subject.setBannedImports(Collections.singletonList("java.util.**"));
        assertThatExceptionOfType(EnforcerRuleException.class)
                .isThrownBy(() -> {
                    this.subject.execute(this.helper);
                });
    }

    @Test
    void testExcludedByBasePackage() throws Exception {
        this.subject.setBasePackage("foo.bar");
        this.subject.setBannedImports(Collections.singletonList("java.util.**"));
        this.subject.execute(this.helper);
    }

    @Test
    void testExcludedByMultipleBasePackages() throws Exception {
        this.subject.setBasePackages(Arrays.asList("foo.bar"));
        this.subject.setBannedImports(Collections.singletonList("java.util.**"));
        this.subject.execute(this.helper);
    }

    @Test
    void testExcludedClass() throws Exception {
        this.subject.setBannedImports(Collections.singletonList("java.util.**"));
        this.subject.setExclusions(Collections.singletonList("de.skuzzle.**"));
        this.subject.execute(this.helper);
    }

    @Test
    void testAllowImport() throws Exception {
        this.subject.setBannedImports(Collections.singletonList("java.util.**"));
        this.subject.setAllowedImports(Collections.singletonList("java.util.ArrayList"));
        this.subject.execute(this.helper);
    }

    @Test
    void testConsistentConfigurationBasePackage1() throws Exception {
        this.subject.setBasePackage("**");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> {
                    this.subject.setBasePackages(Arrays.asList("*", "**"));
                });
    }

    @Test
    void testConsistentConfigurationBasePackage2() throws Exception {
        this.subject.setBasePackages(Arrays.asList("*", "**"));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> {
                    this.subject.setBasePackage("**");
                });
    }

    @Test
    void testConsistentConfigurationBannedImport1() throws Exception {
        this.subject.setBannedImport("**");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> {
                    this.subject.setBannedImports(Arrays.asList("**", "*"));
                });
    }

    @Test
    void testConsistentConfigurationBannedImport2() throws Exception {
        this.subject.setBannedImports(Arrays.asList("**", "*"));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> {
                    this.subject.setBannedImport("**");
                });
    }

    @Test
    void testConsistentConfigurationAllowedImport1() throws Exception {
        this.subject.setAllowedImport("**");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> {
                    this.subject.setAllowedImports(Arrays.asList("**", "*"));
                });
    }

    @Test
    void testConsistentConfigurationAllowedImport2() throws Exception {
        this.subject.setAllowedImports(Arrays.asList("**", "*"));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> {
                    this.subject.setAllowedImport("**");
                });
    }

    @Test
    void testConsistentConfigurationExclusion1() throws Exception {
        this.subject.setExclusion("**");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> {
                    this.subject.setExclusions(Arrays.asList("**", "*"));
                });
    }

    @Test
    void testConsistentConfigurationExclusion2() throws Exception {
        this.subject.setExclusions(Arrays.asList("**", "*"));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> {
                    this.subject.setExclusion("**");
                });
    }

    @Test
    void testConsistentAllowedMatchesBanned() throws Exception {
        this.subject.setBasePackages(
                Arrays.asList("de.skuzzle.test.**", "de.skuzzle.enforcer.**"));
        this.subject.setBannedImports(Arrays.asList("java.util.*"));
        this.subject.setAllowedImports(Arrays.asList("java.util.ArrayList"));
        subject.execute(helper);
    }

    @Test
    void testConsistentConfigurationIllegalBufferSize() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> subject.setCommentLineBufferSize(0));
    }
}
