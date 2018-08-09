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
     * @param settings Context information for the analyzer.
     * @return The analyzer.
     */
    public static SourceTreeAnalyzer getInstance(AnalyzerSettings settings) {
        final LineSupplier lineSupplier = new SkipCommentsLineSupplier(
                settings.getSourceFileCharset(),
                settings.getCommentLineBufferSize());

        final ImportMatcher matcher = new ImportMatcherImpl(lineSupplier);
        return new SourceTreeAnalyzerImpl(settings, matcher);
    }

    /**
     * Analyzes all java classes found recursively in the given root directories for
     * matches of banned imports.
     *
     * @param group The banned import.
     * @return The result of analyzing the given source files.
     */
    AnalyzeResult analyze(BannedImportGroup group);
}
