package de.skuzzle.enforcer.restrictimports.parser;

import java.util.Objects;

import de.skuzzle.enforcer.restrictimports.util.Preconditions;
import de.skuzzle.enforcer.restrictimports.util.StringRepresentation;

/**
 * Represents an import statement that has been discovered while parsing a source file.
 */
public final class ImportStatement {

    private static final String STATIC_IMPORT_PREFIX = "static ";

    private final String importName;
    private final int line;
    private final boolean staticImport;

    public ImportStatement(String importName, int line) {
        Preconditions.checkArgument(importName != null && !importName.isEmpty(), "importName must not be empty");
        Preconditions.checkArgument(!importName.startsWith("import "),
                "importName should be the raw package name without 'import ' prefix but was: '%s'", importName);
        Preconditions.checkArgument(importName.trim().equals(importName),
                "importName has leading or trailing spaces: '%s'", importName);
        Preconditions.checkArgument(line > 0, "line numbers should be 1-based and not start at 0");

        this.importName = importName;
        this.line = line;
        this.staticImport = importName.startsWith(STATIC_IMPORT_PREFIX);
    }

    /**
     * The physical line within the source file in which the import has occurred. Number
     * is always 1-based!
     *
     * @return The line number of the matched imports.
     */
    public int getLine() {
        return line;
    }

    public String getImportName() {
        return importName;
    }

    public String getFqcn() {
        if (this.staticImport) {
            return importName.substring(STATIC_IMPORT_PREFIX.length());
        }
        return importName;
    }

    @Override
    public String toString() {
        return StringRepresentation.ofInstance(this)
                .add("import", importName)
                .add("line", line)
                .add("static", staticImport)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(importName, line, staticImport);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof ImportStatement
                && Objects.equals(this.line, ((ImportStatement) obj).line)
                && Objects.equals(this.staticImport, ((ImportStatement) obj).staticImport)
                && Objects.equals(this.importName, ((ImportStatement) obj).importName);
    }
}
