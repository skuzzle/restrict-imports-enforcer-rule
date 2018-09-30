package de.skuzzle.enforcer.restrictimports.formatting;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.skuzzle.enforcer.restrictimports.analyze.AnalyzeResult;
import de.skuzzle.enforcer.restrictimports.analyze.BannedImportGroup;
import de.skuzzle.enforcer.restrictimports.analyze.MatchedFile;
import de.skuzzle.enforcer.restrictimports.analyze.MatchedImport;

class MatchFormatterImpl implements MatchFormatter {

    static final MatchFormatter INSTANCE = new MatchFormatterImpl();

    @Override
    public String formatMatches(Collection<Path> roots, AnalyzeResult analyzeResult) {
        final StringBuilder b = new StringBuilder("\nBanned imports detected:\n");

        final Map<BannedImportGroup, List<MatchedFile>> matchesByGroup = analyzeResult.getFileMatches().stream()
                .collect(Collectors.groupingBy(MatchedFile::getMatchedBy));

        matchesByGroup.forEach((group, matches) -> {
            final String message = group.getReason();
            if (message != null && !message.isEmpty()) {
                b.append("Reason: ").append(message).append("\n");
            }
            analyzeResult.getFileMatches().forEach(fileMatch -> {
                b.append("\tin file: ")
                        .append(relativize(roots, fileMatch.getSourceFile()))
                        .append("\n");
                fileMatch.getMatchedImports().forEach(match -> appendMatch(match, b));
            });
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

    private void appendMatch(MatchedImport match, StringBuilder b) {
        b.append("\t\t")
                .append(match.getMatchedString())
                .append(" (Line: ")
                .append(match.getImportLine())
                .append(", Matched by: ")
                .append(match.getMatchedBy())
                .append(")\n");
    }

}
