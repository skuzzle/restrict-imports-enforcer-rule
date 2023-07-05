package org.apache.maven.plugins.enforcer;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Collections;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.junit.jupiter.api.Test;

public class ExceptionFormattingTest {

    private final RestrictImports subject = new RestrictImports();
    private final EnforcerRuleHelper helper = MockMavenProject.fromStaticTestFile().enforcerRuleHelper();

    @Test
    void testFormatWithReason() throws Exception {
        this.subject.setBannedImports(Collections.singletonList("java.util.**"));
        this.subject.setReason("Some reason");

        assertThatExceptionOfType(EnforcerRuleException.class)
                .isThrownBy(() -> this.subject.execute(helper))
                .withMessageContaining("\nBanned imports detected:\n\n" +
                        "Reason: Some reason\n" +
                        "\tin file: SampleJavaFile.java\n" +
                        "\t\tjava.util.ArrayList \t(Line: 3, Matched by: java.util.**)\n\nAnalysis of 1 file took");
    }
}
