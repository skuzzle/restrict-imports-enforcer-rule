package de.skuzzle.enforcer.restrictimports.parser;

import java.util.Objects;

import de.skuzzle.enforcer.restrictimports.util.Preconditions;
import de.skuzzle.enforcer.restrictimports.util.StringRepresentation;

/**
 * Represents an import statement that has been discovered while parsing a source file.
 */
public final class ImportStatement {

    private static final String IMPORT_PREFIX = "import ";
    private static final String STATIC_PREFIX = "static ";
    private final String importName;
    private final int line;
    private final ImportType importType;

    public ImportStatement(String importName, int line, ImportType importType) {
        Preconditions.checkArgument(importName != null && !importName.isEmpty(), "importName must not be empty");
        Preconditions.checkArgument(!importName.startsWith(IMPORT_PREFIX),
                "importName should be the raw package name without 'import ' prefix but was: '%s'", importName);
        Preconditions.checkArgument(!importName.startsWith(STATIC_PREFIX),
                "importName should be the raw package name without 'static ' prefix but was: '%s'", importName);
        Preconditions.checkArgument(importName.trim().equals(importName),
                "importName has leading or trailing spaces: '%s'", importName);
        Preconditions.checkArgument(line > 0, "line numbers should be 1-based and not start at 0");
        Preconditions.checkArgument(importType != null, "importType must not be null");

        this.importName = importName;
        this.line = line;
        this.importType = importType;
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

    /**
     * Returns the import name including the 'static ' prefix if this represents a static
     * import.
     *
     * @return The full import name.
     */
    public String getImportName() {
        if (importType == ImportType.STATIC_IMPORT) {
            return STATIC_PREFIX + importName;
        }
        return importName;
    }

    public String getFqcn() {
        return importName;
    }

    /**
     * Whether this is an 'artificial' import that has been created from a full qualified
     * type reference within the source rather than from a real import statement.
     *
     * @return Whether this represents a full qualified type use.
     */
    public boolean isQualifiedTypeUsage() {
        return importType == ImportType.QUALIFIED_TYPE_USE;
    }

    @Override
    public String toString() {
        return StringRepresentation.ofInstance(this)
                .add("import", importName)
                .add("line", line)
                .add("importType", importType)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(importName, line, importType);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof ImportStatement
                && Objects.equals(this.line, ((ImportStatement) obj).line)
                && Objects.equals(this.importType, ((ImportStatement) obj).importType)
                && Objects.equals(this.importName, ((ImportStatement) obj).importName);
    }
}
