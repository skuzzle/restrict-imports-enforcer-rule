package de.skuzzle.enforcer.restrictimports;

class Match {

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
