package de.skuzzle.enforcer.restrictimports.analyze;

import java.nio.file.Path;

/**
 * Represents a single match of a banned import within a java source file.
 *
 * @author Simon Taddiken
 */
public class Match {

    private final Path sourceFile;
    private final int importLine;
    private final String matchedString;

    Match(Path sourceFile, int importLine, String matchedString) {
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

}
