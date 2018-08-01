package de.skuzzle.enforcer.restrictimports.analyze;

import java.util.List;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;

import com.google.common.collect.ImmutableList;

public class BannedImportGroup {

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

    public static class Builder {
        private List<PackagePattern> basePackages = ImmutableList.of();
        private List<PackagePattern> bannedImports = ImmutableList.of();
        private List<PackagePattern> allowedImports = ImmutableList.of();
        private List<PackagePattern> excludedClasses = ImmutableList.of();
        private String reason;

        public Builder withBasePackages(List<PackagePattern> basePackages) {
            this.basePackages = basePackages;
            return this;
        }

        public Builder withBannedImports(List<PackagePattern> bannedImports) {
            this.bannedImports = bannedImports;
            return this;
        }

        public Builder withAllowedImports(List<PackagePattern> allowedImports) {
            this.allowedImports = allowedImports;
            return this;
        }

        public Builder withExcludedClasses(List<PackagePattern> excludedClasses) {
            this.excludedClasses = excludedClasses;
            return this;
        }

        public Builder withReason(String reason) {
            this.reason = reason;
            return this;
        }

        public BannedImportGroup build() throws EnforcerRuleException {
            final BannedImportGroup group = new BannedImportGroup(basePackages,
                    bannedImports, allowedImports,
                    excludedClasses, reason);
            GroupConsistencyChecker.getInstance().checkGroupConsistency(group);
            return group;
        }
    }
}
