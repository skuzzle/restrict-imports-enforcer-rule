package de.skuzzle.enforcer.restrictimports.analyze;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Holds the matches that were found within a single java source file. Instances can be
 * constructed using {@link #forSourceFile(Path)}.
 *
 * @author Simon Taddiken
 */
public final class MatchedFile {

    private final Path sourceFile;
    private final boolean testFile;
    private final List<MatchedImport> matchedImports;
    private final BannedImportGroup matchedBy;

    MatchedFile(Path sourceFile, boolean testFile, List<MatchedImport> matchedImports, BannedImportGroup matchedBy) {
        this.sourceFile = sourceFile;
        this.testFile = testFile;
        this.matchedImports = matchedImports;
        this.matchedBy = matchedBy;
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
     * Whether this matched file is a test file.
     *
     * @return Whether this is a test file.
     */
    public boolean isTestFile() {
        return testFile;
    }

    /**
     * The matches found in this file.
     *
     * @return The matches.
     */
    public List<MatchedImport> getMatchedImports() {
        return this.matchedImports;
    }

    /**
     * Returns the group that contains the banned import that caused the match in this
     * file.
     *
     * @return The group.
     */
    public BannedImportGroup getMatchedBy() {
        return this.matchedBy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceFile, testFile, matchedImports, matchedBy);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof MatchedFile
                && Objects.equals(testFile, ((MatchedFile) obj).testFile)
                && Objects.equals(sourceFile, ((MatchedFile) obj).sourceFile)
                && Objects.equals(matchedImports, ((MatchedFile) obj).matchedImports)
                && Objects.equals(matchedBy, ((MatchedFile) obj).matchedBy);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("sourceFile", this.sourceFile)
                .add("testFile", testFile)
                .add("matchedImports", matchedImports)
                .add("matchedBy", matchedBy)
                .toString();
    }

    public static class Builder {
        private final Path sourceFile;
        private final List<MatchedImport> matchedImports = new ArrayList<>();
        private BannedImportGroup matchedBy;
        private boolean testFile;

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

        /**
         * Sets the testFile flag.
         *
         * @param testFile Whether this is a test file.
         * @return This builder.
         */
        public Builder isTestFile(boolean testFile) {
            this.testFile = testFile;
            return this;
        }

        /**
         * Creates the {@link MatchedFile} instance.
         *
         * @return The instance.
         */
        public MatchedFile build() {
            Preconditions.checkArgument(sourceFile != null, "sourceFile must not be null");
            Preconditions.checkArgument(matchedBy != null, "matchedBy must not be null for MatchedFile %s", sourceFile);
            return new MatchedFile(sourceFile, testFile, matchedImports, matchedBy);
        }
    }
}
