package de.skuzzle.enforcer.restrictimports.analyze;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.junit.Test;

public class GroupConsistencyCheckerImplTest {

    private final GroupConsistencyChecker subject = GroupConsistencyChecker.getInstance();

    @Test(expected = EnforcerRuleException.class)
    public void testNoBannedImports() throws Exception {
        final BannedImportGroup group = mock(BannedImportGroup.class);
        when(group.getBannedImports()).thenReturn(Collections.emptyList());
        when(group.getBasePackages()).thenReturn(Collections.emptyList());
        when(group.getAllowedImports()).thenReturn(Collections.emptyList());

        this.subject.checkGroupConsistency(group);
    }

    @Test(expected = EnforcerRuleException.class)
    public void testInconsistentAllowedImports() throws Exception {
        final BannedImportGroup group = mock(BannedImportGroup.class);
        when(group.getBannedImports())
                .thenReturn(Arrays.asList(PackagePattern.parse("dont.care.**")));
        when(group.getBasePackages())
                .thenReturn(Arrays.asList(PackagePattern.parse("com.foo.**")));
        when(group.getAllowedImports())
                .thenReturn(Arrays.asList(PackagePattern.parse("foo.**")));

        this.subject.checkGroupConsistency(group);
    }

    @Test(expected = EnforcerRuleException.class)
    public void testInconsistentExclusions() throws Exception {
        final BannedImportGroup group = mock(BannedImportGroup.class);
        when(group.getBannedImports())
                .thenReturn(Arrays.asList(PackagePattern.parse("dont.care.**")));
        when(group.getBasePackages())
                .thenReturn(Arrays.asList(PackagePattern.parse("base.package.**")));
        when(group.getExcludedClasses())
                .thenReturn(Arrays.asList(PackagePattern.parse("foo.bar.**")));

        this.subject.checkGroupConsistency(group);
    }
}
