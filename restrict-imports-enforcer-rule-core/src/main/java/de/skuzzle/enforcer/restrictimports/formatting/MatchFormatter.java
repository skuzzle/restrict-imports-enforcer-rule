package de.skuzzle.enforcer.restrictimports.formatting;

import java.nio.file.Path;
import java.util.Collection;

import de.skuzzle.enforcer.restrictimports.analyze.AnalyzeResult;

/**
 * For formatting the result of the banned import analysis.
 */
public interface MatchFormatter {

    static MatchFormatter getInstance() {
        return MatchFormatterImpl.INSTANCE;
    }

    String formatMatches(AnalyzeResult analyzeResult);
}
