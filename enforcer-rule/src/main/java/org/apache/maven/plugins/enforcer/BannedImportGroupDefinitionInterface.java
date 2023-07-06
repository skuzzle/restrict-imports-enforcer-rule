package org.apache.maven.plugins.enforcer;

import java.util.List;

interface BannedImportGroupDefinitionInterface {

    void setBasePackage(String basePackage);

    void setBasePackages(List<String> basePackages);

    void setBannedImport(String bannedImport);

    void setBannedImports(List<String> bannedPackages);

    void setAllowedImport(String allowedImport);

    void setAllowedImports(List<String> allowedImports);

    void setExclusion(String exclusion);

    void setExclusions(List<String> exclusions);

    void setReason(String reason);
}
