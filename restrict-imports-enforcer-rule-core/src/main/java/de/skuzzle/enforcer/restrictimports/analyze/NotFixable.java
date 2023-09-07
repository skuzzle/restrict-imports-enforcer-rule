package de.skuzzle.enforcer.restrictimports.analyze;

import java.util.List;

import de.skuzzle.enforcer.restrictimports.util.StringRepresentation;

public final class NotFixable {

    private final PackagePattern in;
    private final List<PackagePattern> imports;

    private NotFixable(PackagePattern in, List<PackagePattern> imports) {
        this.in = in;
        this.imports = imports;
    }

    public static NotFixable of(PackagePattern in, List<PackagePattern> imports) {
        return new NotFixable(in, imports);
    }

    public boolean matchesFqcn(String fqcn) {
        return in.matches(fqcn);
    }

    public boolean matchesImport(String importName) {
        return imports.stream().anyMatch(pattern -> pattern.matches(importName));
    }

    public void checkConsistency() {
        if (imports.isEmpty()) {
            throw new BannedImportDefinitionException("No 'not fixable' imports defined for " + in);
        }
    }

    @Override
    public String toString() {
        return StringRepresentation.ofInstance(this)
                .add("in", in)
                .add("imports", imports)
                .toString();
    }
}
