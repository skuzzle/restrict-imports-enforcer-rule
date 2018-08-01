package de.skuzzle.enforcer.restrictimports.analyze;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface MatchFormatter {

    public static MatchFormatter getInstance() {
        return MatchFormatterImpl.INSTANCE;
    }

    String formatMatches(Collection<Path> roots, Map<Path, List<Match>> matchesPerFile,
            BannedImportGroup group);
}
