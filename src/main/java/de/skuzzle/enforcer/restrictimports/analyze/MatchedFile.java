package de.skuzzle.enforcer.restrictimports.analyze;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * Holds the matches that were found within a single java source file. Instances can be
 * constructed using {@link #forSourceFile(Path)}.
 *
 * @author Simon Taddiken
 */
public final class MatchedFile {

    private final Path sourceFile;
    private final List<MatchedImport> matchedImports;

    MatchedFile(Path sourceFile, List<MatchedImport> matchedImports) {
        this.sourceFile = sourceFile;
        this.matchedImports = matchedImports;
    }

    /**
     * Constructs a MatchedFile
     *
     * @param sourceFile The path to the java source file.
     * @return A Builder for furhter configuration of the MatchedFile instance.
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
     * The matches found in this file.
     *
     * @return The matches.
     */
    public List<MatchedImport> getMatchedImports() {
        return this.matchedImports;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceFile, matchedImports);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof MatchedFile
                && Objects.equals(sourceFile, ((MatchedFile) obj).sourceFile)
                && Objects.equals(matchedImports, ((MatchedFile) obj).matchedImports);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("sourceFile", this.sourceFile)
                .add("matchedImports", matchedImports)
                .toString();
    }

    public static class Builder {
        private final Path sourceFile;
        private final List<MatchedImport> matchedImports = new ArrayList<>();

        private Builder(Path sourceFile) {
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
            this.matchedImports.add(
                    new MatchedImport(importLine, matchedString, matchedBy));
            return this;
        }

        /**
         * Creates the {@link MatchedFile} instance.
         *
         * @return The instance.
         */
        public MatchedFile build() {
            return new MatchedFile(sourceFile, matchedImports);
        }
    }
}
