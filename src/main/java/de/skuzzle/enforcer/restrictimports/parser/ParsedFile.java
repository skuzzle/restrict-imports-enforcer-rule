package de.skuzzle.enforcer.restrictimports.parser;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

public final class ParsedFile {

    private static final String STATIC_IMPORT_PREFIX = "static ";

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

    public static final class ImportStatement {

        private final String importName;
        private final int line;
        private final boolean staticImport;

        public ImportStatement(String importName, int line) {
            Preconditions.checkArgument(importName != null && !importName.isEmpty(), "importName must not be empty");
            Preconditions.checkArgument(!importName.startsWith("import "),
                    "importName should be the raw package name without 'import ' prefix but was: '%s'", importName);
            this.importName = importName;
            this.line = line;
            this.staticImport = importName.startsWith(STATIC_IMPORT_PREFIX);
        }

        public int getLine() {
            return line;
        }

        public String getImportName() {
            return importName;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("import", importName)
                    .add("line", line)
                    .toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(importName, line, staticImport);
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof  ImportStatement
                    && Objects.equals(this.line, ((ImportStatement) obj).line)
                    && Objects.equals(this.staticImport, ((ImportStatement) obj).staticImport)
                    && Objects.equals(this.importName, ((ImportStatement) obj).importName);
        }
    }
}
