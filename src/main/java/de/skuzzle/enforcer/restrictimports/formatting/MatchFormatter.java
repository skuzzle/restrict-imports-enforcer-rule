package de.skuzzle.enforcer.restrictimports.formatting;

import de.skuzzle.enforcer.restrictimports.analyze.AnalyzeResult;

import java.nio.file.Path;
import java.util.Collection;

/**
 * For formatting the result of the banned import analysis.
 */
public interface MatchFormatter {

    static MatchFormatter getInstance() {
        return MatchFormatterImpl.INSTANCE;
    }

    String formatMatches(Collection<Path> roots, AnalyzeResult analyzeResult);
}
