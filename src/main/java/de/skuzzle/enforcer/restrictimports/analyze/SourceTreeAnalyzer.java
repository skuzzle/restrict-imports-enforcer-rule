package de.skuzzle.enforcer.restrictimports.analyze;

/**
 * Analyzes the whole source tree for matches of banned imports.
 *
 * @author Simon Taddiken
 */
public interface SourceTreeAnalyzer {

    /**
     * Creates a new {@link SourceTreeAnalyzer} instance.
     *
     * @return The analyzer.
     */
    static SourceTreeAnalyzer getInstance() {
        return new SourceTreeAnalyzerImpl();
    }

    /**
     * Analyzes all java classes found recursively in the given root directories for
     * matches of banned imports.
     *
     * @param settings Context information for performing the analysis.
     * @param groups The banned imports.
     * @return The result of analyzing the given source files.
     */
    AnalyzeResult analyze(AnalyzerSettings settings, BannedImportGroups groups);
}
