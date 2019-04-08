package de.skuzzle.enforcer.restrictimports.analyze;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.skuzzle.enforcer.restrictimports.parser.ImportStatement;
import de.skuzzle.enforcer.restrictimports.parser.ParsedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects banned import matches from a single source file.
 *
 * @author Simon Taddiken
 */
class ImportAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportAnalyzer.class);

    /**
     * Collects all imports that are banned within the given source file.
     *
     * @param sourceFile The parsed file to check for banned imports..
     * @param groups The groups of banned imports to check the file against. From all
     *            groups, the one with the most specific base pattern match is chosen.
     * @return a {@link MatchedFile} holds information about the found matches. Returns an
     *         empty optional if no matches were found.
     */
    public Optional<MatchedFile> matchFile(ParsedFile sourceFile, BannedImportGroups groups) {
        LOGGER.trace("Analyzing {} for banned imports", sourceFile);

        BannedImportGroup group = groups.selectGroupFor(sourceFile.getFqcn()).orElse(null);
        if (group == null) {
            return Optional.empty();
        }

        final List<MatchedImport> matches = new ArrayList<>();
        for (ImportStatement importStmt : sourceFile.getImports()) {
            group.ifImportIsBanned(importStmt.getImportName())
                    .map(bannedImport -> new MatchedImport(importStmt.getLine(), importStmt.getImportName(), bannedImport))
                    .ifPresent(matches::add);
        }
        if (matches.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new MatchedFile(sourceFile.getPath(), matches, group));
    }
}
