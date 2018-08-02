package de.skuzzle.enforcer.restrictimports.analyze;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * Holds the matches that were found within a single java source file.
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

        public Builder withMatchAt(int importLine, String matchedString) {
            this.matchedImports.add(
                    new MatchedImport(sourceFile, importLine, matchedString));
            return this;
        }

        public MatchedFile build() {
            return new MatchedFile(sourceFile, matchedImports);
        }
    }
}
