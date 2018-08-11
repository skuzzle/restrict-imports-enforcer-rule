package de.skuzzle.enforcer.restrictimports.analyze;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

/**
 * Holds the user configured information of what imports should be banned including all
 * further meta information like base packages, allowed imports and excluded classes.
 *
 * @author Simon Taddiken
 */
public final class BannedImportGroup {

    private final List<PackagePattern> basePackages;
    private final List<PackagePattern> bannedImports;
    private final List<PackagePattern> allowedImports;
    private final List<PackagePattern> excludedClasses;
    private final String reason;

    private BannedImportGroup(List<PackagePattern> basePackages,
            List<PackagePattern> bannedImports,
            List<PackagePattern> allowedImports,
            List<PackagePattern> excludedClasses,
            String reason) {
        this.basePackages = basePackages;
        this.bannedImports = bannedImports;
        this.allowedImports = allowedImports;
        this.excludedClasses = excludedClasses;
        this.reason = reason;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<PackagePattern> getBasePackages() {
        return this.basePackages;
    }

    public List<PackagePattern> getBannedImports() {
        return this.bannedImports;
    }

    public List<PackagePattern> getAllowedImports() {
        return this.allowedImports;
    }

    public List<PackagePattern> getExcludedClasses() {
        return this.excludedClasses;
    }

    public String getReason() {
        return this.reason;
    }

    @Override
    public int hashCode() {
        return Objects.hash(basePackages, bannedImports, allowedImports, excludedClasses,
                reason);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof BannedImportGroup
                && Objects.equals(basePackages, ((BannedImportGroup) obj).basePackages)
                && Objects.equals(bannedImports, ((BannedImportGroup) obj).bannedImports)
                && Objects.equals(allowedImports,
                        ((BannedImportGroup) obj).allowedImports)
                && Objects.equals(excludedClasses,
                        ((BannedImportGroup) obj).excludedClasses)
                && Objects.equals(reason, ((BannedImportGroup) obj).reason);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("basePackages", this.basePackages)
                .add("bannedImports", this.bannedImports)
                .add("allowedImports", this.allowedImports)
                .add("excludedClasses", this.excludedClasses)
                .add("reason", this.reason)
                .toString();
    }

    public static class Builder {
        private List<PackagePattern> basePackages = ImmutableList.of();
        private List<PackagePattern> bannedImports = ImmutableList.of();
        private List<PackagePattern> allowedImports = ImmutableList.of();
        private List<PackagePattern> excludedClasses = ImmutableList.of();
        private String reason;

        private Builder() {
            // hidden
        }

        public Builder withBasePackages(List<PackagePattern> basePackages) {
            this.basePackages = basePackages;
            return this;
        }

        public Builder withBasePackages(String... basePackages) {
            return withBasePackages(PackagePattern.parseAll(Arrays.asList(basePackages)));
        }

        public Builder withBannedImports(List<PackagePattern> bannedImports) {
            this.bannedImports = bannedImports;
            return this;
        }

        public Builder withBannedImports(String... bannedImports) {
            return withBannedImports(
                    PackagePattern.parseAll(Arrays.asList(bannedImports)));
        }

        public Builder withAllowedImports(List<PackagePattern> allowedImports) {
            this.allowedImports = allowedImports;
            return this;
        }

        public Builder withAllowedImports(String... allowedImports) {
            return withAllowedImports(
                    PackagePattern.parseAll(Arrays.asList(allowedImports)));
        }

        public Builder withExcludedClasses(List<PackagePattern> excludedClasses) {
            this.excludedClasses = excludedClasses;
            return this;
        }

        public Builder withExcludedClasses(String... excludedClasses) {
            return withExcludedClasses(
                    PackagePattern.parseAll(Arrays.asList(excludedClasses)));
        }

        public Builder withReason(String reason) {
            this.reason = reason;
            return this;
        }

        public BannedImportGroup build() throws EnforcerRuleException {
            final BannedImportGroup group = new BannedImportGroup(basePackages,
                    bannedImports, allowedImports,
                    excludedClasses, reason);
            checkGroupConsistency(group);
            return group;
        }

        private void checkGroupConsistency(BannedImportGroup group)
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
}
