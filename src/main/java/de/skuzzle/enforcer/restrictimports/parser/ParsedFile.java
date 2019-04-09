package de.skuzzle.enforcer.restrictimports.parser;

import com.google.common.base.MoreObjects;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a source file that has been parsed for import statements.
 */
public final class ParsedFile {

    private final Path path;
    private final String declaredPackage;
    private final String fqcn;
    private final Collection<ImportStatement> imports;

    public ParsedFile(Path path, String declaredPackage, String fqcn, Collection<ImportStatement> imports) {
        this.path = path;
        this.declaredPackage = declaredPackage;
        this.fqcn = fqcn;
        this.imports = imports;
    }

    public Path getPath() {
        return path;
    }

    public Collection<ImportStatement> getImports() {
        return imports;
    }

    public String getFqcn() {
        return fqcn;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("path", path)
                .add("declaredPackage", declaredPackage)
                .add("fqcn", fqcn)
                .add("imports", imports)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, declaredPackage, fqcn, imports);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof ParsedFile
                && Objects.equals(path, ((ParsedFile) obj).path)
                && Objects.equals(declaredPackage, ((ParsedFile) obj).declaredPackage)
                && Objects.equals(fqcn, ((ParsedFile) obj).fqcn)
                && Objects.equals(imports, ((ParsedFile) obj).imports);
    }

}
