package de.skuzzle.enforcer.restrictimports;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;

public class BannedImportGroup {

    private PackagePattern basePackage = PackagePattern.parse("**");
    private List<PackagePattern> bannedImports = new ArrayList<>();
    private List<PackagePattern> allowedImports = new ArrayList<>();
    private List<PackagePattern> excludedClasses = new ArrayList<>();

    public final PackagePattern getBasePackage() {
        return this.basePackage;
    }

    public final void setBasePackage(String basePackage) {
        this.basePackage = PackagePattern.parse(basePackage);
    }

    public final List<PackagePattern> getBannedImports() {
        return this.bannedImports;
    }

    public final void setBannedImports(List<String> bannedPackages) {
        checkArgument(bannedPackages != null && !bannedPackages.isEmpty(),
                "bannedPackages must not be empty");
        this.bannedImports = PackagePattern.parseAll(bannedPackages);
    }

    public final List<PackagePattern> getAllowedImports() {
        return this.allowedImports;
    }

    public final void setAllowedImports(List<String> allowedImports) {
        this.allowedImports = PackagePattern.parseAll(allowedImports);
    }

    public final List<PackagePattern> getExcludedClasses() {
        return this.excludedClasses;
    }

    public final void setExcludedClasses(List<String> excludedClasses) {
        this.excludedClasses = PackagePattern.parseAll(excludedClasses);
    }
}
