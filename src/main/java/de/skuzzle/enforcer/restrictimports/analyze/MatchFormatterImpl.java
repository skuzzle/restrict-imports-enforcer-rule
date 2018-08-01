package de.skuzzle.enforcer.restrictimports.analyze;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class MatchFormatterImpl implements MatchFormatter {

    static final MatchFormatter INSTANCE = new MatchFormatterImpl();

    @Override
    public String formatMatches(Collection<Path> roots,
            Map<Path, List<Match>> matchesPerFile,
            BannedImportGroup group) {
        final StringBuilder b = new StringBuilder("\nBanned imports detected:\n");
        final String message = group.getReason();
        if (message != null && !message.isEmpty()) {
            b.append("Reason: ").append(message).append("\n");
        }
        matchesPerFile.forEach((fileName, matches) -> {
            b.append("\tin file: ")
                    .append(relativize(roots, fileName))
                    .append("\n");
            matches.forEach(match -> appendMatch(match, b));
        });
        return b.toString();
    }

    private static Path relativize(Collection<Path> roots, Path path) {
        for (final Path root : roots) {
            if (path.startsWith(root)) {
                return root.relativize(path);
            }
        }
        return path;
    }

    private void appendMatch(Match match, StringBuilder b) {
        b.append("\t\t")
                .append(match.getMatchedString())
                .append(" (Line: ")
                .append(match.getImportLine())
                .append(")\n");
    }

}
