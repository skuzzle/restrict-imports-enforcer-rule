package de.skuzzle.enforcer.restrictimports.analyze;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Collects banned import matches from a single java source file.
 *
 * @author Simon Taddiken
 */
interface ImportMatcher {

    /**
     * Collects all imports that are banned within the given java source file.
     *
     * @param sourceFile The path to a java source file to check for banned imports..
     * @param groups The groups of banned imports to check the file against. From all
     *            groups, the one with the most specific base pattern match is chosen.
     * @return a {@link MatchedFile} holds information about the found matches. Returns an
     *         empty optional if no matches were found.
     */
    Optional<MatchedFile> matchFile(Path sourceFile, BannedImportGroups groups);
}
