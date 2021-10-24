package de.skuzzle.enforcer.restrictimports.analyze;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import de.skuzzle.enforcer.restrictimports.util.StringRepresentation;

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
    private final List<PackagePattern> exclusions;
    private final String reason;

    private BannedImportGroup(List<PackagePattern> basePackages,
            List<PackagePattern> bannedImports,
            List<PackagePattern> allowedImports,
            List<PackagePattern> exclusions,
            String reason) {
        this.basePackages = basePackages;
        this.bannedImports = bannedImports;
        this.allowedImports = allowedImports;
        this.exclusions = exclusions;
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

    public List<PackagePattern> getExclusions() {
        return this.exclusions;
    }

    public boolean exclusionMatches(String fqcn) {
        return matchesAnyPattern(fqcn, exclusions);
    }

    private boolean matchesAnyPattern(String packageName,
            Collection<PackagePattern> patterns) {
        return patterns.stream()
                .anyMatch(pattern -> pattern.matches(packageName));
    }

    public Optional<String> getReason() {
        return Optional.ofNullable(this.reason).filter(s -> !s.isEmpty());
    }

    @Override
    public int hashCode() {
        return Objects.hash(basePackages, bannedImports, allowedImports, exclusions, reason);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof BannedImportGroup
                && Objects.equals(basePackages, ((BannedImportGroup) obj).basePackages)
                && Objects.equals(bannedImports, ((BannedImportGroup) obj).bannedImports)
                && Objects.equals(allowedImports, ((BannedImportGroup) obj).allowedImports)
                && Objects.equals(exclusions, ((BannedImportGroup) obj).exclusions)
                && Objects.equals(reason, ((BannedImportGroup) obj).reason);
    }

    @Override
    public String toString() {
        return StringRepresentation.ofInstance(this)
                .add("basePackages", this.basePackages)
                .add("bannedImports", this.bannedImports)
                .add("allowedImports", this.allowedImports)
                .add("exclusions", this.exclusions)
                .add("reason", this.reason)
                .toString();
    }

    public static class Builder {
        private List<PackagePattern> basePackages = Collections.emptyList();
        private List<PackagePattern> bannedImports = Collections.emptyList();
        private List<PackagePattern> allowedImports = Collections.emptyList();
        private List<PackagePattern> exclusions = Collections.emptyList();
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

        public Builder withExclusions(List<PackagePattern> exclusions) {
            this.exclusions = exclusions;
            return this;
        }

        public Builder withExclusions(String... exclusions) {
            return withExclusions(
                    PackagePattern.parseAll(Arrays.asList(exclusions)));
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
                    exclusions, reason);
            checkGroupConsistency(group);
            return group;
        }

        private void checkGroupConsistency(BannedImportGroup group) {
            checkAmbiguous(group);
            checkBannedImportsPresent(group);
            allowedImportMustMatchBannedPattern(group);
            checkBasePackageNotStatic(group);
            checkExclusionNotStatic(group);
            exclusionsMustMatchBasePattern(group);
        }

        private void checkAmbiguous(BannedImportGroup group) {
            checkAmbiguous(group.getBasePackages(), "base package");
            checkAmbiguous(group.getBannedImports(), "banned import");
            checkAmbiguous(group.getAllowedImports(), "allowed import");
            checkAmbiguous(group.getExclusions(), "exclusion");
        }

        private void checkAmbiguous(Collection<PackagePattern> patterns, String errorTemplate) {
            for (final PackagePattern outer : patterns) {
                for (final PackagePattern inner : patterns) {
                    if (inner != outer && (inner.matches(outer))) {
                        throw new BannedImportDefinitionException(String
                                .format("There are ambiguous %s definitions: %s, %s", errorTemplate, inner, outer));
                    }
                }
            }
        }

        private void checkBasePackageNotStatic(BannedImportGroup group) {
            if (group.getBasePackages().stream().anyMatch(PackagePattern::isStatic)) {
                throw new BannedImportDefinitionException("Base packages must not be static");
            }
        }

        private void checkExclusionNotStatic(BannedImportGroup group) {
            if (group.getExclusions().stream().anyMatch(PackagePattern::isStatic)) {
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
            for (final PackagePattern excludedClass : group.getExclusions()) {
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
