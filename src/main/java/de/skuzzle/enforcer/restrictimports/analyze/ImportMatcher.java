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
     * @param file The file to check.
     * @param group The group of banned imports to check the file against.
     * @return A stream of found matches.
     */
    Stream<MatchedImport> matchFile(Path file, BannedImportGroup group);
}
