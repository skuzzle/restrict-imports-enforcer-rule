package de.skuzzle.enforcer.restrictimports.rule;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.skuzzle.enforcer.restrictimports.analyze.BannedImportGroup;
import de.skuzzle.enforcer.restrictimports.analyze.PackagePattern;

public class BannedImportGroupDefinition {

    private static final PackagePattern DEFAULT_BASE_PACKAGE = PackagePattern.parse("**");

    private PackagePattern basePackage = DEFAULT_BASE_PACKAGE;
    private List<PackagePattern> basePackages = new ArrayList<>();

    private PackagePattern bannedImport = null;
    private List<PackagePattern> bannedImports = new ArrayList<>();

    private PackagePattern allowedImport = null;
    private List<PackagePattern> allowedImports = new ArrayList<>();

    private PackagePattern exclusion = null;
    private List<PackagePattern> exclusions = new ArrayList<>();

    private String reason;

    public BannedImportGroup createGroupFromPluginConfiguration() {
        return BannedImportGroup.builder()
                .withBasePackages(assembleList(this.basePackage, this.basePackages))
                .withBannedImports(assembleList(this.bannedImport, this.bannedImports))
                .withAllowedImports(assembleList(this.allowedImport, this.allowedImports))
                .withExcludedClasses(assembleList(this.exclusion, this.exclusions))
                .withReason(reason)
                .build();
    }

    private List<PackagePattern> assembleList(PackagePattern single,
            List<PackagePattern> multi) {
        if (single == null) {
            return multi;
        } else {
            return Collections.singletonList(single);
        }
    }

    public final void setBasePackage(String basePackage) {
        checkArgument(this.basePackages.isEmpty(),
                "Configuration error: you should either specify a single base package using <basePackage> or multiple "
                        + "base packages using <basePackages> but not both");
        this.basePackage = PackagePattern.parse(basePackage);
    }

    public final void setBasePackages(List<String> basePackages) {
        checkArgument(this.basePackage == DEFAULT_BASE_PACKAGE,
                "Configuration error: you should either specify a single base package using <basePackage> or multiple "
                        + "base packages using <basePackages> but not both");
        checkArgument(basePackages != null && !basePackages.isEmpty(),
                "bannedPackages must not be empty");
        this.basePackage = null;
        this.basePackages = PackagePattern.parseAll(basePackages);
    }

    public void setBannedImport(String bannedImport) {
        checkArgument(this.bannedImports.isEmpty(),
                "Configuration error: you should either specify a single banned import using <bannedImport> or multiple "
                        + "banned imports using <bannedImports> but not both");
        this.bannedImport = PackagePattern.parse(bannedImport);
    }

    public final void setBannedImports(List<String> bannedPackages) {
        checkArgument(this.bannedImport == null,
                "Configuration error: you should either specify a single banned import using <bannedImport> or multiple "
                        + "banned imports using <bannedImports> but not both");
        checkArgument(bannedPackages != null && !bannedPackages.isEmpty(),
                "bannedPackages must not be empty");
        this.bannedImport = null;
        this.bannedImports = PackagePattern.parseAll(bannedPackages);
    }

    public final void setAllowedImport(String allowedImport) {
        checkArgument(this.allowedImports.isEmpty(),
                "Configuration error: you should either specify a single allowed import using <allowedImport> or multiple "
                        + "allowed imports using <allowedImports> but not both");
        this.allowedImport = PackagePattern.parse(allowedImport);
    }

    public final void setAllowedImports(List<String> allowedImports) {
        checkArgument(this.allowedImport == null,
                "Configuration error: you should either specify a single allowed import using <allowedImport> or multiple "
                        + "allowed imports using <allowedImports> but not both");
        this.allowedImports = PackagePattern.parseAll(allowedImports);
    }

    public final void setExclusion(String exclusion) {
        checkArgument(this.exclusions.isEmpty(),
                "Configuration error: you should either specify a single exclusion using <exclusion> or multiple "
                        + "exclusions using <exclusions> but not both");
        this.exclusion = PackagePattern.parse(exclusion);
    }

    public final void setExclusions(List<String> exclusions) {
        checkArgument(this.exclusion == null,
                "Configuration error: you should either specify a single exclusion using <exclusion> or multiple "
                        + "exclusions using <exclusions> but not both");
        this.exclusions = PackagePattern.parseAll(exclusions);
    }

    public final void setReason(String reason) {
        this.reason = reason;
    }
}
