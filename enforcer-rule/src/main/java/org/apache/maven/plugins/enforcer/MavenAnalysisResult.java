package org.apache.maven.plugins.enforcer;

import de.skuzzle.enforcer.restrictimports.analyze.AnalyzeResult;
import de.skuzzle.enforcer.restrictimports.util.Preconditions;

final class MavenAnalysisResult {
    private final AnalyzeResult result;

    private MavenAnalysisResult(AnalyzeResult result) {
        Preconditions.checkArgument(result != null, "result must not be null");
        this.result = result;
    }

    public static MavenAnalysisResult from(AnalyzeResult result) {
        return new MavenAnalysisResult(result);
    }
}
