package de.skuzzle.enforcer.restrictimports.analyze;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    public boolean basePackageMatches(String fqcn) {
        return matchesAnyPattern(fqcn, basePackages);
    }

    public List<PackagePattern> getBannedImports() {
        return this.bannedImports;
    }

    public Optional<PackagePattern> ifImportIsBanned(String importName) {
        return bannedImports.stream()
                .filter(bannedImport -> bannedImport.matches(importName))
                .filter(result -> !allowedImportMatches(importName))
                .findFirst();
    }

    public List<PackagePattern> getAllowedImports() {
        return this.allowedImports;
    }

    public boolean allowedImportMatches(String importName) {
        return matchesAnyPattern(importName, allowedImports);
    }

    public List<PackagePattern> getExcludedClasses() {
        return this.excludedClasses;
    }

    public boolean exclusionMatches(String fqcn) {
        return matchesAnyPattern(fqcn, excludedClasses);
    }

    private boolean matchesAnyPattern(String packageName,
            Collection<PackagePattern> patterns) {
        return patterns.stream()
                .anyMatch(pattern -> pattern.matches(packageName));
    }

    public String getReason() {
        return this.reason;
    }

    @Override
    public int hashCode() {
        return Objects.hash(basePackages, bannedImports, allowedImports, excludedClasses, reason);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof BannedImportGroup
                && Objects.equals(basePackages, ((BannedImportGroup) obj).basePackages)
                && Objects.equals(bannedImports, ((BannedImportGroup) obj).bannedImports)
                && Objects.equals(allowedImports, ((BannedImportGroup) obj).allowedImports)
                && Objects.equals(excludedClasses, ((BannedImportGroup) obj).excludedClasses)
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

        /**
         * Assembles the {@link BannedImportGroup} from this builder.
         *
         * @return The group.
         * @throws BannedImportDefinitionException If the group definition is not
         *             consistent.
         */
        public BannedImportGroup build() {
            final BannedImportGroup group = new BannedImportGroup(basePackages,
                    bannedImports, allowedImports,
                    excludedClasses, reason);
            checkGroupConsistency(group);
            return group;
        }

        private void checkGroupConsistency(BannedImportGroup group) {
            checkBannedImportsPresent(group);
            allowedImportMustMatchBannedPattern(group);
            checkBasePackageNotStatic(group);
            checkExclusionNotStatic(group);
            exclusionsMustMatchBasePattern(group);
        }

        private void checkBasePackageNotStatic(BannedImportGroup group) {
            if (group.getBasePackages().stream().anyMatch(PackagePattern::isStatic)) {
                throw new BannedImportDefinitionException("Base packages must not be static");
            }
        }

        private void checkExclusionNotStatic(BannedImportGroup group) {
            if (group.getExcludedClasses().stream().anyMatch(PackagePattern::isStatic)) {
                throw new BannedImportDefinitionException("Exclusions must not be static");
            }
        }

        private void checkBannedImportsPresent(BannedImportGroup group) {
            if (group.getBannedImports().isEmpty()) {
                throw new BannedImportDefinitionException("There are no banned imports specified");
            }
        }

        private void allowedImportMustMatchBannedPattern(BannedImportGroup group) {
            for (final PackagePattern allowedImport : group.getAllowedImports()) {
                final boolean matches = group.getBannedImports().stream()
                        .anyMatch(bannedPackage -> bannedPackage.matches(allowedImport));
                if (!matches) {
                    throw new BannedImportDefinitionException(String.format(
                            "The allowed import pattern '%s' does not match any banned package.",
                            allowedImport));
                }
            }
        }

        private void exclusionsMustMatchBasePattern(BannedImportGroup group) {
            for (final PackagePattern excludedClass : group.getExcludedClasses()) {
                final boolean matches = group.getBasePackages().stream()
                        .anyMatch(basePackage -> basePackage.matches(excludedClass));
                if (!matches) {
                    throw new BannedImportDefinitionException(String.format(
                            "The exclusion pattern '%s' does not match any base package.",
                            excludedClass));
                }
            }
        }
    }
}
