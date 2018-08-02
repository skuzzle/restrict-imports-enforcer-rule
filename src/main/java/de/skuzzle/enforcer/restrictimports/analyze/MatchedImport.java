package de.skuzzle.enforcer.restrictimports.analyze;

import java.nio.file.Path;
import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * Represents a single match of a banned import within a java source file.
 *
 * @author Simon Taddiken
 */
public final class MatchedImport {

    private final Path sourceFile;
    private final int importLine;
    private final String matchedString;

    MatchedImport(Path sourceFile, int importLine, String matchedString) {
        this.sourceFile = sourceFile;
        this.importLine = importLine;
        this.matchedString = matchedString;
    }

    public final Path getSourceFile() {
        return this.sourceFile;
    }

    public final int getImportLine() {
        return this.importLine;
    }

    public final String getMatchedString() {
        return this.matchedString;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.sourceFile, this.importLine, this.matchedString);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof MatchedImport
                && Objects.equals(this.sourceFile, ((MatchedImport) obj).sourceFile)
                && Objects.equals(this.importLine, ((MatchedImport) obj).importLine)
                && Objects.equals(this.matchedString, ((MatchedImport) obj).matchedString);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("sourceFile", this.sourceFile)
                .add("importLine", this.importLine)
                .add("matchedString", matchedString)
                .toString();
    }
}
