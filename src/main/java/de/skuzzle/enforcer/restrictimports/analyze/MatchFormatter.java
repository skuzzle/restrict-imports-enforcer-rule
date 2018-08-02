package de.skuzzle.enforcer.restrictimports.analyze;

import java.nio.file.Path;
import java.util.Collection;

public interface MatchFormatter {

    public static MatchFormatter getInstance() {
        return MatchFormatterImpl.INSTANCE;
    }

    String formatMatches(Collection<Path> roots, AnalyzeResult analyzeResult,
            BannedImportGroup group);
}
