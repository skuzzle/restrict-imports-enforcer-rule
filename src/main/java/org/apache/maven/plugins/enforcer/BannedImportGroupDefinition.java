package org.apache.maven.plugins.enforcer;

import static de.skuzzle.enforcer.restrictimports.util.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.skuzzle.enforcer.restrictimports.analyze.BannedImportGroup;
import de.skuzzle.enforcer.restrictimports.analyze.PackagePattern;

public class BannedImportGroupDefinition {

    // for the consistency check we need to distinguish the static default '**' from an
    // explicitly configured '**', this
    // only works by comparing with '=='. As the static String '**' would be interned by
    // the compiler, we
    // need to explicitly wrap it in a new instance here (at least for the test to be
    // happy, might still work 'in production').
    private static final String DEFAULT_BASE_PACKAGE = new String("**");

    private String basePackage = DEFAULT_BASE_PACKAGE;
    private List<String> basePackages = new ArrayList<>();

    private String bannedImport = null;
    private List<String> bannedImports = new ArrayList<>();

    private String allowedImport = null;
    private List<String> allowedImports = new ArrayList<>();

    private String exclusion = null;
    private List<String> exclusions = new ArrayList<>();

    private String reason;

    public BannedImportGroup createGroupFromPluginConfiguration(boolean ignoreStatic) {
        return BannedImportGroup.builder()
                .withBasePackages(assembleList(ignoreStatic, this.basePackage, this.basePackages))
                .withBannedImports(assembleList(ignoreStatic, this.bannedImport, this.bannedImports))
                .withAllowedImports(assembleList(ignoreStatic, this.allowedImport, this.allowedImports))
                .withExclusions(assembleList(ignoreStatic, this.exclusion, this.exclusions))
                .withReason(reason)
                .build();
    }

    /**
     * Determines whether the user modified at least a single field within this
     * definition.
     *
     * @return Whether there are user made changes.
     */
    public boolean hasInput() {
        return basePackage != DEFAULT_BASE_PACKAGE
                || !basePackages.isEmpty()
                || bannedImport != null
                || !bannedImports.isEmpty()
                || allowedImport != null
                || !allowedImports.isEmpty()
                || exclusion != null
                || !exclusions.isEmpty();
    }

    private List<PackagePattern> assembleList(boolean ignoreStatic, String single,
            List<String> multi) {
        if (single == null) {
            return PackagePattern.parseAll(multi, ignoreStatic);
        } else {
            return Collections.singletonList(PackagePattern.parse(single, ignoreStatic));
        }
    }

    public void setBasePackage(String basePackage) {
        checkArgument(this.basePackages.isEmpty(),
                "Configuration error: you should either specify a single base package using <basePackage> or multiple "
                        + "base packages using <basePackages> but not both");
        this.basePackage = basePackage;
    }

    public void setBasePackages(List<String> basePackages) {
        checkArgument(this.basePackage == DEFAULT_BASE_PACKAGE,
                "Configuration error: you should either specify a single base package using <basePackage> or multiple "
                        + "base packages using <basePackages> but not both");
        checkArgument(basePackages != null && !basePackages.isEmpty(),
                "bannedPackages must not be empty");
        this.basePackage = null;
        this.basePackages = basePackages;
    }

    public void setBannedImport(String bannedImport) {
        checkArgument(this.bannedImports.isEmpty(),
                "Configuration error: you should either specify a single banned import using <bannedImport> or multiple "
                        + "banned imports using <bannedImports> but not both");
        checkArgument(this.bannedImport == null,
                "If you want to specify multiple banned imports you have to wrap them in a <bannedImports> tag");
        this.bannedImport = bannedImport;
    }

    public void setBannedImports(List<String> bannedPackages) {
        checkArgument(this.bannedImport == null,
                "Configuration error: you should either specify a single banned import using <bannedImport> or multiple "
                        + "banned imports using <bannedImports> but not both");
        checkArgument(bannedPackages != null && !bannedPackages.isEmpty(),
                "bannedPackages must not be empty");
        this.bannedImport = null;
        this.bannedImports = bannedPackages;
    }

    public void setAllowedImport(String allowedImport) {
        checkArgument(this.allowedImports.isEmpty(),
                "Configuration error: you should either specify a single allowed import using <allowedImport> or multiple "
                        + "allowed imports using <allowedImports> but not both");
        checkArgument(this.allowedImport == null,
                "If you want to specify multiple allowed imports you have to wrap them in a <allowedImports> tag");
        this.allowedImport = allowedImport;
    }

    public void setAllowedImports(List<String> allowedImports) {
        checkArgument(this.allowedImport == null,
                "Configuration error: you should either specify a single allowed import using <allowedImport> or multiple "
                        + "allowed imports using <allowedImports> but not both");
        this.allowedImports = allowedImports;
    }

    public void setExclusion(String exclusion) {
        checkArgument(this.exclusions.isEmpty(),
                "Configuration error: you should either specify a single exclusion using <exclusion> or multiple "
                        + "exclusions using <exclusions> but not both");
        checkArgument(this.exclusion == null,
                "If you want to specify multiple exclusions you have to wrap them in a <exclusions> tag");
        this.exclusion = exclusion;
    }

    public void setExclusions(List<String> exclusions) {
        checkArgument(this.exclusion == null,
                "Configuration error: you should either specify a single exclusion using <exclusion> or multiple "
                        + "exclusions using <exclusions> but not both");
        this.exclusions = exclusions;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
