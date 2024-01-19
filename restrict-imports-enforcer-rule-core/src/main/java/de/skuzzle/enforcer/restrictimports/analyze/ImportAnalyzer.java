package de.skuzzle.enforcer.restrictimports.analyze;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.skuzzle.enforcer.restrictimports.parser.Annotation;
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
    Optional<MatchedFile> matchFile(ParsedFile sourceFile, BannedImportGroups groups) {
        LOGGER.trace("Analyzing {} for banned imports", sourceFile);

        final List<Warning> warnings = warningsFrom(sourceFile);
        if (sourceFile.isFailedToParse()) {
            LOGGER.trace("Skipping analysis because source file failed to parse: {}", sourceFile);
            return Optional.of(MatchedFile.forSourceFile(sourceFile.getPath())
                    .withFailedToParse(true)
                    .withWarnings(warnings)
                    .build());
        }

        final BannedImportGroup group = groups.selectGroupFor(sourceFile.getFqcn())
                .orElse(null);
        if (group == null) {
            if (!warnings.isEmpty()) {
                LOGGER.trace("No rule group matched {}, but warnings were found: {}", sourceFile, warnings);
                return Optional.of(MatchedFile.forSourceFile(sourceFile.getPath()).withWarnings(warnings).build());
            } else {
                LOGGER.trace("No rule group matched {}", sourceFile);
                return Optional.empty();
            }

        }
        LOGGER.trace("Selected {} for {}", group, sourceFile);

        final List<MatchedImport> matches = new ArrayList<>();
        for (final ImportStatement importStmt : sourceFile.getImports()) {
            group.ifImportIsBanned(sourceFile.getFqcn(), importStmt.getImportName())
                    .map(bannedImport -> new MatchedImport(importStmt.getLine(), importStmt.getImportName(),
                            bannedImport))
                    .ifPresent(matches::add);
        }
        if (matches.isEmpty() && warnings.isEmpty()) {
            return Optional.empty();
        }
        final MatchedFile matchedFile = new MatchedFile(sourceFile.getPath(), matches, group, warnings, false);
        LOGGER.debug("Found banned import matches or warnings: {}", matchedFile);
        return Optional.of(matchedFile);
    }

    private List<Warning> warningsFrom(ParsedFile sourceFile) {
        return sourceFile.getAnnotations().stream()
                .map(Annotation::getMessage)
                .map(Warning::withMessage)
                .collect(Collectors.toList());
    }
}
