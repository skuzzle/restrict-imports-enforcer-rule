package org.apache.maven.plugins.enforcer;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.skuzzle.enforcer.restrictimports.rule.RestrictImports;

public class RestrictImportsTest {

    private static final List<String> SOURCE_ROOTS = Arrays.asList("/src/main/java",
            "/target/generated-sources/main/java/");

    private final EnforcerRuleHelper helper = mock(EnforcerRuleHelper.class);
    private final Log log = mock(Log.class);
    private final MavenProject mavenProject = mock(MavenProject.class);

    private final RestrictImports subject = new RestrictImports();

    @BeforeEach
    public void setUp() throws Exception {
        when(this.helper.getLog()).thenReturn(this.log);
        when(this.helper.evaluate("${project}")).thenReturn(this.mavenProject);

        when(this.mavenProject.getProperties()).thenReturn(new Properties());

        final List<String> paths = SOURCE_ROOTS.stream()
                .map(this::absolutePath)
                .collect(Collectors.toList());
        when(this.mavenProject.getCompileSourceRoots()).thenReturn(paths);
    }

    @Test
    void testRestrictImportsNoFailure() throws Exception {
        this.subject.setBannedImport("foo.com.**");
        this.subject.execute(this.helper);
    }

    @Test
    void testRestrictFailure() {
        this.subject.setBannedImports(Collections.singletonList("java.util.**"));
        assertThatExceptionOfType(EnforcerRuleException.class)
                .isThrownBy(() -> this.subject.execute(this.helper));
    }

    @Test
    void testRestrictFailureForFileUnderGeneratedSources() {
        this.subject.setBannedImports(Collections.singletonList("java.io.**"));
        assertThatExceptionOfType(EnforcerRuleException.class)
                .isThrownBy(() -> this.subject.execute(this.helper));
    }

    @Test
    void testRestrictImportsNoFailureForFileUnderExcludedSourceRoot() throws Exception {
        this.subject.setExcludedSourceRoot(new File(absolutePath(SOURCE_ROOTS.get(1))));
        this.subject.setBannedImports(Collections.singletonList("java.io.**"));
        this.subject.execute(this.helper);
    }

    @Test
    void testRestrictImportsNoFailureForFileUnderExcludedSourceRoots() throws Exception {
        this.subject.setExcludedSourceRoots(Collections.singletonList(new File(absolutePath(SOURCE_ROOTS.get(1)))));
        this.subject.setBannedImports(Collections.singletonList("java.io.**"));
        this.subject.execute(this.helper);
    }

    @Test
    void testConsistentConfigurationExcludeTestCodeAndCompileCode() throws Exception {
        this.subject.setIncludeCompileCode(false);
        this.subject.setIncludeTestCode(false);
        assertThatExceptionOfType(EnforcerRuleException.class)
                .isThrownBy(() -> {
                    subject.execute(helper);
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
    void testConsistentConfigurationSetMultipleBannedImportWithoutGroupingTag() throws Exception {
        this.subject.setBannedImport("foo.com.**");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> this.subject.setBannedImport("foo2.com.**"));
    }

    @Test
    void testConsistentConfigurationSetMultipleAllowedImportWithoutGroupingTag() throws Exception {
        this.subject.setAllowedImport("foo.com.**");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> this.subject.setAllowedImport("foo2.com.**"));
    }

    @Test
    void testConsistentConfigurationSetMultipleExclusionstWithoutGroupingTag() throws Exception {
        this.subject.setExclusion("foo.com.**");
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> this.subject.setExclusion("foo2.com.**"));
    }

    @Test
    void testConsistentConfigurationSpecifyGroupsLast() throws Exception {
        this.subject.setBannedImport("**");
        final BannedImportGroupDefinition group1 = new BannedImportGroupDefinition();
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> this.subject.setGroups(Arrays.asList(group1)));
    }

    @Test
    void testConsistentConfigurationSpecifyGroupsFirst() throws Exception {
        final BannedImportGroupDefinition group1 = new BannedImportGroupDefinition();
        this.subject.setGroups(Arrays.asList(group1));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> this.subject.setBannedImport("**"));
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
    void testConsistentConfigurationExcludedSourceRoot1() {
        this.subject.setExcludedSourceRoot(new File("foo"));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(
                        () -> this.subject.setExcludedSourceRoots(Arrays.asList(new File("/foo"), new File("/bar"))));
    }

    @Test
    void testConsistentConfigurationExcludedSourceRoot2() {
        this.subject.setExcludedSourceRoots(Arrays.asList(new File("/foo"), new File("/bar")));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> this.subject.setExcludedSourceRoot(new File("/foo")));
    }

    private String absolutePath(String path) {
        final URL url = getClass().getResource(path);
        try {
            return new File(url.toURI()).toPath().toString();
        } catch (final URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
