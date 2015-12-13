package de.skuzzle.enforcer.restrictimports;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Analyzes the whole source tree for matches of banned imports.
 *
 * @author Simon Taddiken
 */
public interface SourceTreeAnalyzer {

    /**
     * Analyzes all java classes found recursively in the given root directories
     * for matches of banned imports.
     *
     * @param roots The source directories.
     * @param groups The banned imports.
     * @return A map of file names to the matches found within that file.
     */
    Map<String, List<Match>> analyze(Stream<Path> roots,
            Collection<BannedImportGroup> groups);
}
