package de.skuzzle.enforcer.restrictimports.analyze;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import java.util.Objects;

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
        Preconditions.checkArgument(matchedString != null && !matchedString.isEmpty(),
                "matched String must not be empty");
        Preconditions.checkArgument(matchedBy != null, "matchedBy should not be null");
        Preconditions.checkArgument(importLine > 0, "line numbers should be 1-based and not start at 0");
        this.importLine = importLine;
        this.matchedString = matchedString;
        this.matchedBy = matchedBy;
    }

    /**
     * The physical line within the source file in which the import has been matched.
     * Number is always 1-based!
     *
     * @return The line number of the matched imports.
     */
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
                && Objects.equals(this.matchedString, ((MatchedImport) obj).matchedString)
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
