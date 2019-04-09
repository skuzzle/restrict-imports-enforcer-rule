package de.skuzzle.enforcer.restrictimports.cd;

import de.skuzzle.enforcer.restrictimports.parser.ParsedFile;

public interface PackageCycleDetector {

    static PackageCycleDetector getInstance() {
        return new PackageCycleDetectorImpl();
    }

    CycleAnalyzeResult analyzeForCycles(Iterable<ParsedFile> parsedFiles);
}
