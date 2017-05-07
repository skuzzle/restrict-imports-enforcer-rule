package de.skuzzle.enforcer.restrictimports.model;

/**
 * Represents a single match of a banned import within a java source file.
 *
 * @author Simon Taddiken
 */
public class Match {

    private final String sourceFile;
    private final int importLine;
    private final String matchedString;

    public Match(String sourceFile, int importLine, String matchedString) {
        this.sourceFile = sourceFile;
        this.importLine = importLine;
        this.matchedString = matchedString;
    }

    public final String getSourceFile() {
        return this.sourceFile;
    }

    public final int getImportLine() {
        return this.importLine;
    }

    public final String getMatchedString() {
        return this.matchedString;
    }

}
