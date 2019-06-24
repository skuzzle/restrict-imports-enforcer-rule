package de.skuzzle.enforcer.restrictimports.formatting;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.skuzzle.enforcer.restrictimports.analyze.AnalyzeResult;
import de.skuzzle.enforcer.restrictimports.analyze.BannedImportGroup;
import de.skuzzle.enforcer.restrictimports.analyze.MatchedFile;
import de.skuzzle.enforcer.restrictimports.analyze.MatchedImport;

class MatchFormatterImpl implements MatchFormatter {

    static final MatchFormatter INSTANCE = new MatchFormatterImpl();

    @Override
    public String formatMatches(Collection<Path> roots, AnalyzeResult analyzeResult) {
        final StringBuilder b = new StringBuilder();

        if (analyzeResult.bannedImportsInCompileCode()) {
            b.append("\nBanned imports detected:\n\n");

            final Map<BannedImportGroup, List<MatchedFile>> srcMatchesByGroup = analyzeResult.srcMatchesByGroup();
            formatGroupedMatches(roots, b, srcMatchesByGroup);
        }

        if (analyzeResult.bannedImportsInTestCode()) {
            b.append("\nBanned imports detected in TEST code:\n\n");
            final Map<BannedImportGroup, List<MatchedFile>> testMatchesByGroup = analyzeResult.testMatchesByGroup();
            formatGroupedMatches(roots, b, testMatchesByGroup);
        }

        final long seconds = analyzeResult.getDuration() / 1000;
        b.append("\nAnalysis took ").append(seconds).append(" seconds\n");

        return b.toString();
    }

    private void formatGroupedMatches(Collection<Path> roots, StringBuilder b,
            Map<BannedImportGroup, List<MatchedFile>> matchesByGroup) {
        matchesByGroup.forEach((group, matches) -> {
            final String message = group.getReason();
            if (message != null && !message.isEmpty()) {
                b.append("Reason: ").append(message).append("\n");
            }
            matches.forEach(fileMatch -> {
                b.append("\tin file").append(": ")
                        .append(relativize(roots, fileMatch.getSourceFile()))
                        .append("\n");
                fileMatch.getMatchedImports().forEach(match -> appendMatch(match, b));
            });
        });
    }

    private static Path relativize(Collection<Path> roots, Path path) {
        return roots.stream()
                .filter(path::startsWith)
                .map(root -> root.relativize(path))
                .findFirst()
                .orElse(path);
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
