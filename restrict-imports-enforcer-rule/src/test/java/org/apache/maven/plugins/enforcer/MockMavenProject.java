package org.apache.maven.plugins.enforcer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import org.apache.maven.project.MavenProject;

public class MockMavenProject {
    private final MavenProject mavenProject = mock(MavenProject.class);
    private final Properties properties = new Properties();

    private final Path srcMainJava;
    private final Path srcFile;

    private MockMavenProject(String srcFile) {
        try {
            final URL url = getClass().getResource(srcFile);
            final File f = new File(url.toURI());
            this.srcFile = f.toPath();
            this.srcMainJava = this.srcFile.getParent();

            when(this.mavenProject.getCompileSourceRoots())
                    .thenReturn(Collections.singletonList(srcMainJava.toString()));
            when(this.mavenProject.getProperties()).thenReturn(properties);

        } catch (final Exception e) {
            throw new IllegalStateException("Error while setting up Mock maven project", e);
        }
    }

    public static MockMavenProject fromStaticTestFile() {
        return new MockMavenProject("/src/main/java/SampleJavaFile.java");
    }

    public MavenProject mavenProject() {
        return this.mavenProject;
    }

    public Collection<Path> srcDir() {
        return Collections.singleton(srcMainJava);
    }

    public Path testSourceFile() {
        return srcFile;
    }
}
