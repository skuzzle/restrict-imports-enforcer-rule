package de.skuzzle.enforcer.restrictimports.analyze;

import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * Represents a single match of a banned import within a java source file.
 *
 * @author Simon Taddiken
 */
public final class MatchedImport {

    private final int importLine;
    private final String matchedString;
    private final PackagePattern matchedBy;

    MatchedImport(int importLine, String matchedString, PackagePattern matchedBy) {
        this.importLine = importLine;
        this.matchedString = matchedString;
        this.matchedBy = matchedBy;
    }

    public int getImportLine() {
        return this.importLine;
    }

    public String getMatchedString() {
        return this.matchedString;
    }

    public PackagePattern getMatchedBy() {
        return this.matchedBy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.importLine, this.matchedString, this.matchedBy);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof MatchedImport
                && Objects.equals(this.importLine, ((MatchedImport) obj).importLine)
                && Objects.equals(this.matchedString,
                        ((MatchedImport) obj).matchedString)
                && Objects.equals(this.matchedBy, ((MatchedImport) obj).matchedBy);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("importLine", this.importLine)
                .add("matchedString", matchedString)
                .add("matchedBy", matchedBy)
                .toString();
    }
}
