package de.skuzzle.enforcer.restrictimports.analyze;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;

class GroupConsistencyCheckerImpl implements GroupConsistencyChecker {

    static final GroupConsistencyChecker INSTANCE = new GroupConsistencyCheckerImpl();

    @Override
    public void checkGroupConsistency(BannedImportGroup group)
            throws EnforcerRuleException {
        checkBannedImportsPresent(group);
        allowedImportMustMatchBannedPattern(group);
        exclusionsMustMatchBasePattern(group);
    }

    private void checkBannedImportsPresent(BannedImportGroup group)
            throws EnforcerRuleException {
        if (group.getBannedImports().isEmpty()) {
            throw new EnforcerRuleException("There are no banned imports specified");
        }
    }

    private void allowedImportMustMatchBannedPattern(BannedImportGroup group)
            throws EnforcerRuleException {
        for (final PackagePattern allowedImport : group.getAllowedImports()) {
            final boolean matches = group.getBannedImports().stream()
                    .anyMatch(bannedPackage -> bannedPackage.matches(allowedImport));
            if (!matches) {
                throw new EnforcerRuleException(String.format(
                        "The allowed import pattern '%s' does not match any banned package.",
                        allowedImport));
            }
        }
    }

    private void exclusionsMustMatchBasePattern(BannedImportGroup group)
            throws EnforcerRuleException {
        for (final PackagePattern excludedClass : group.getExcludedClasses()) {
            final boolean matches = group.getBasePackages().stream()
                    .anyMatch(basePackage -> basePackage.matches(excludedClass));
            if (!matches) {
                throw new EnforcerRuleException(String.format(
                        "The exclusion pattern '%s' does not match any base package.",
                        excludedClass));
            }
        }
    }
}
