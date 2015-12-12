package de.skuzzle.enforcer.restrictimports;

import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Collects banned import matches from a java source file.
 *
 * @author Simon Taddiken
 */
interface ImportMatcher {

    /**
     * Collects all imports that are banned within the given java source file.
     *
     * @param file The file to check.
     * @param bannedImports Patterns of banned imports.
     * @param allowed Patterns of allowed imports (that are only applied if an
     *            import has already been detected as banned).
     * @return A stream of found matches.
     */
    Stream<Match> matchFile(Path file, Collection<PackagePattern> bannedImports,
            Collection<PackagePattern> allowed);
}
