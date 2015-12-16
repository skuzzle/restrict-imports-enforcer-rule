package de.skuzzle.enforcer.restrictimports;

import java.util.List;

public class BannedImportGroup {

    private final PackagePattern basePackage;
    private final List<PackagePattern> bannedImports;
    private final List<PackagePattern> allowedImports;
    private final List<PackagePattern> excludedClasses;

    public BannedImportGroup(PackagePattern basePackage,
            List<PackagePattern> bannedImports, List<PackagePattern> allowedImports,
            List<PackagePattern> excludedClasses) {
        this.basePackage = basePackage;
        this.bannedImports = bannedImports;
        this.allowedImports = allowedImports;
        this.excludedClasses = excludedClasses;
    }

    public final PackagePattern getBasePackage() {
        return this.basePackage;
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
}
