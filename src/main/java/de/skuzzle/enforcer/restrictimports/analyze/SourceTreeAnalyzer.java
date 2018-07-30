package de.skuzzle.enforcer.restrictimports.analyze;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;

/**
 * Analyzes the whole source tree for matches of banned imports.
 *
 * @author Simon Taddiken
 */
public interface SourceTreeAnalyzer {

    /**
     * Checks whether the given group is consistent with respect to all user input.
     *
     * @param group The group to check.
     * @throws EnforcerRuleException If the group is not consistent.
     */
    void checkGroupConsistency(BannedImportGroup group) throws EnforcerRuleException;

    /**
     * Analyzes all java classes found recursively in the given root directories for
     * matches of banned imports.
     *
     * @param roots The source directories.
     * @param group The banned import.
     * @return A map of file names to the matches found within that file.
     */
    Map<String, List<Match>> analyze(Stream<Path> roots, BannedImportGroup group);
}
