package de.skuzzle.enforcer.restrictimports.parser;

import de.skuzzle.enforcer.restrictimports.util.Preconditions;
import de.skuzzle.enforcer.restrictimports.util.StringRepresentation;

import java.nio.file.Path;
import java.util.*;

/**
 * Represents a source file that has been parsed for import statements.
 */
public final class ParsedFile {

    private final Path path;
    private final String declaredPackage;
    private final String fqcn;
    private final Collection<ImportStatement> imports;

    private final boolean failedToParse;
    private final List<Annotation> annotations;

    private ParsedFile(Path path, String declaredPackage, String fqcn, Collection<ImportStatement> imports, boolean failedToParse, List<Annotation> annotations) {
        Preconditions.checkArgument(path != null, "path must not be null");
        Preconditions.checkArgument(declaredPackage != null, "declaredPackage must not be null");
        Preconditions.checkArgument(fqcn != null, "fqcn must not be null");
        Preconditions.checkArgument(imports != null, "imports must not be null");
        Preconditions.checkArgument(annotations != null, "annotations must not be null");
        Preconditions.checkState(!failedToParse || !annotations.isEmpty(), "annotations must not be empty if failedToParse was true");
        this.path = path;
        this.declaredPackage = declaredPackage;
        this.fqcn = fqcn;
        this.imports = imports;
        this.failedToParse = failedToParse;
        this.annotations = annotations;
    }

    /**
     * Creates a {@link ParsedFile} for a source file that was parsed successfully.
     *
     * @param path The path of the source file.
     * @param declaredPackage The package from the source file's <code>package</code> declaration
     * @param fqcn The full qualified name of the class represented by the source file.
     * @param imports The detected import statements within the source file.
     * @return The ParsedFile.
     * @since 2.2.0
     */
    public static ParsedFile successful(Path path, String declaredPackage, String fqcn, Collection<ImportStatement> imports) {
        return new ParsedFile(path, declaredPackage, fqcn, imports, false, Collections.emptyList());
    }

    /**
     * Creates a {@link ParsedFile} for the situation where the file could not be
     * parsed at all (technical problem occurred while parsing).
     *
     * @param path The file that could not be parsed.
     * @param annotations At least one annotation that roughly describes the failure.
     * @return The ParsedFile.
     * @since 2.2.0
     */
    public static ParsedFile failedToParse(Path path, Annotation... annotations) {
        return new ParsedFile(path, "", "",
            Collections.emptyList(), true, Arrays.asList(annotations));
    }

    /**
     * Returns a new {@link ParsedFile} with identical information as this one but merging this file's
     * annotations with the provided annotations.
     *
     * @param furtherAnnotations Annotations to append to the resulting ParsedFile.
     * @return A new ParsedFile.
     * @since 2.2.0
     */
    public ParsedFile andAddAnnotation(Annotation... furtherAnnotations) {
        final List<Annotation> allAnnotations = new ArrayList<>(this.annotations);
        allAnnotations.addAll(Arrays.asList(furtherAnnotations));
        return new ParsedFile(path, declaredPackage, fqcn, imports, failedToParse, allAnnotations);
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

    /**
     * Whether the file could not be parsed at all and we have no information about used imports.
     *
     * @return Whether the file could not be parsed at all.
     * @since 2.2.0
     */
    public boolean isFailedToParse() {
        return failedToParse;
    }

    /**
     * A list of findings in this file apart from banned imports. Those findings will be reported
     * separately in the analysis report but will usually not fail the build.
     *
     * @return Any findings/warnings that were detected while parsing this file.
     * @since 2.2.0
     */
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public String toString() {
        return StringRepresentation.ofInstance(this)
            .add("path", path)
            .add("declaredPackage", declaredPackage)
            .add("fqcn", fqcn)
            .add("imports", imports)
            .add("annotations", annotations)
            .add("failedToParse", failedToParse)
            .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, declaredPackage, fqcn, imports, failedToParse, annotations);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof ParsedFile
            && Objects.equals(path, ((ParsedFile) obj).path)
            && Objects.equals(declaredPackage, ((ParsedFile) obj).declaredPackage)
            && Objects.equals(fqcn, ((ParsedFile) obj).fqcn)
            && Objects.equals(imports, ((ParsedFile) obj).imports)
            && Objects.equals(annotations, ((ParsedFile) obj).annotations)
            && failedToParse == ((ParsedFile) obj).failedToParse;
    }

}
