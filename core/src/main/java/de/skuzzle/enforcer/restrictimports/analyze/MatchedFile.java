package de.skuzzle.enforcer.restrictimports.analyze;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import de.skuzzle.enforcer.restrictimports.util.Preconditions;
import de.skuzzle.enforcer.restrictimports.util.StringRepresentation;

/**
 * Holds the matches that were found within a single source file. Instances can be
 * constructed using {@link #forSourceFile(Path)}.
 *
 * @author Simon Taddiken
 */
public final class MatchedFile {

    private final Path sourceFile;
    private final List<MatchedImport> matchedImports;
    private final BannedImportGroup matchedBy;
    private final List<Warning> warnings;
    private final boolean failedToParse;

    MatchedFile(Path sourceFile, List<MatchedImport> matchedImports, BannedImportGroup matchedBy,
            List<Warning> warnings, boolean failedToParse) {
        this.sourceFile = sourceFile;
        this.matchedImports = matchedImports;
        this.matchedBy = matchedBy;
        this.warnings = warnings;
        this.failedToParse = failedToParse;
    }

    /**
     * Constructs a MatchedFile
     *
     * @param sourceFile The path to the java source file.
     * @return A Builder for further configuration of the MatchedFile instance.
     */
    public static Builder forSourceFile(Path sourceFile) {
        return new Builder(sourceFile);
    }

    /**
     * The java source file containing the matches.
     *
     * @return The java source file.
     */
    public Path getSourceFile() {
        return this.sourceFile;
    }

    /**
     * The matches found in this file. Will be empty in case no banned imports were found
     * but still warnings were detected while analyzing this file.
     *
     * @return The matches.
     */
    public List<MatchedImport> getMatchedImports() {
        return this.matchedImports;
    }

    /**
     * Returns the group that contains the banned import that caused the match in this
     * file.
     * <p>
     * The result will be empty if this file wasn't matched by any group but warnings were
     * found while parsings. In that case the list returned by
     * {@link #getMatchedImports()} will also be empty.
     *
     * @return The group. Will be empty if {@link #hasBannedImports()} is false.
     */
    public Optional<BannedImportGroup> getMatchedBy() {
        return Optional.ofNullable(this.matchedBy);
    }

    /**
     * Whether the file could not be parsed at all. When true, this file will not be
     * matced against any banned import definitions but {@link #getWarnings()} will
     * contain a helpful message why parsing failed.
     *
     * @return Whether we failed to parse the file.
     * @see #getWarnings()
     */
    public boolean isFailedToParse() {
        return failedToParse;
    }

    /**
     *
     * @return
     */
    public List<Warning> getWarnings() {
        return warnings;
    }

    public boolean hasWarning() {
        return !warnings.isEmpty();
    }

    public boolean hasBannedImports() {
        return !matchedImports.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceFile, matchedImports, matchedBy, warnings, failedToParse);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof MatchedFile
                && Objects.equals(sourceFile, ((MatchedFile) obj).sourceFile)
                && Objects.equals(matchedImports, ((MatchedFile) obj).matchedImports)
                && Objects.equals(matchedBy, ((MatchedFile) obj).matchedBy)
                && Objects.equals(warnings, ((MatchedFile) obj).warnings)
                && failedToParse == ((MatchedFile) obj).failedToParse;
    }

    @Override
    public String toString() {
        return StringRepresentation.ofInstance(this)
                .add("sourceFile", this.sourceFile)
                .add("matchedImports", matchedImports)
                .add("matchedBy", matchedBy)
                .add("warnings", warnings)
                .add("failedToParse", failedToParse)
                .toString();
    }

    public static class Builder {
        private final Path sourceFile;
        private final List<MatchedImport> matchedImports = new ArrayList<>();
        private BannedImportGroup matchedBy;

        private boolean failedToParse;

        private final List<Warning> warnings = new ArrayList<>();

        private Builder(Path sourceFile) {
            Preconditions.checkArgument(sourceFile != null, "sourceFile must not be null");
            this.sourceFile = sourceFile;
        }

        /**
         * Records a matched import within this file.
         *
         * @param importLine The physical line number (1 based) at which the match
         *            occurred.
         * @param matchedString The string that was matched.
         * @param matchedBy The {@link PackagePattern} that caused this match.
         * @return This builder.
         */
        public Builder withMatchAt(int importLine, String matchedString,
                PackagePattern matchedBy) {
            this.matchedImports.add(new MatchedImport(importLine, matchedString, matchedBy));
            return this;
        }

        /**
         * Sets the group that contained the banned import that caused a match for this
         * file.
         *
         * @param group The group.
         * @return This builder.
         */
        public Builder matchedBy(BannedImportGroup group) {
            this.matchedBy = group;
            return this;
        }

        public Builder withWarnings(Warning... warnings) {
            return withWarnings(Arrays.asList(warnings));
        }

        public Builder withWarnings(List<Warning> warnings) {
            this.warnings.addAll(warnings);
            return this;
        }

        public Builder withFailedToParse(boolean failedToParse) {
            this.failedToParse = failedToParse;
            return this;
        }

        /**
         * Creates the {@link MatchedFile} instance.
         *
         * @return The instance.
         */
        public MatchedFile build() {
            return new MatchedFile(sourceFile, matchedImports, matchedBy, warnings, failedToParse);
        }
    }
}
