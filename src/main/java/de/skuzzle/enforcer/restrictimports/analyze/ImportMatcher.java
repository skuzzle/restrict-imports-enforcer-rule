package de.skuzzle.enforcer.restrictimports.analyze;

import java.nio.file.Path;
import java.util.stream.Stream;

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
     * @param group The group of banned imports to check the file against.
     * @return A stream of found matches.
     */
    Stream<MatchedImport> matchFile(Path sourceFile, BannedImportGroup group);
}
