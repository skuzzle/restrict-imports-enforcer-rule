package de.skuzzle.enforcer.restrictimports.analyze;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;

/**
 * For checking whether a {@link BannedImportGroup} has been configured correctly in the
 * enforcer plugin configuration.
 *
 * @author Simon Taddiken
 */
public interface GroupConsistencyChecker {

    /**
     * Returns a {@link GroupConsistencyChecker} implementation.
     *
     * @return The consistency checker.
     */
    public static GroupConsistencyChecker getInstance() {
        return GroupConsistencyCheckerImpl.INSTANCE;
    }

    /**
     * Checks whether the given group is consistent with respect to all user input.
     *
     * @param group The group to check.
     * @throws EnforcerRuleException If the group is not consistent.
     */
    void checkGroupConsistency(BannedImportGroup group) throws EnforcerRuleException;
}
