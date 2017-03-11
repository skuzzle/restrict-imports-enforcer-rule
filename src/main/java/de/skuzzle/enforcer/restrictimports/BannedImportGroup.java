package de.skuzzle.enforcer.restrictimports;

import java.util.List;

public class BannedImportGroup {

    private final List<PackagePattern> basePackages;
    private final List<PackagePattern> bannedImports;
    private final List<PackagePattern> allowedImports;
    private final List<PackagePattern> excludedClasses;
    private final String reason;

    public BannedImportGroup(List<PackagePattern> basePackages,
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

    public final List<PackagePattern> getBasePackages() {
        return this.basePackages;
    }

    public final List<PackagePattern> getBannedImports() {
        return this.bannedImports;
    }

    public final List<PackagePattern> getAllowedImports() {
        return this.allowedImports;
    }

    public final List<PackagePattern> getExcludedClasses() {
        return this.excludedClasses;
    }

    public String getReason() {
        return this.reason;
    }
}
