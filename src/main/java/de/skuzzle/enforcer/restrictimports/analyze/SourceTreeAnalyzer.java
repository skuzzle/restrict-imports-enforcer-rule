package de.skuzzle.enforcer.restrictimports.analyze;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

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
    public static SourceTreeAnalyzer getInstance() {
        final IOUtils ioUtils = new IOUtils();
        final ImportMatcher matcher = new ImportMatcherImpl(Files::lines);
        return new SourceTreeAnalyzerImpl(matcher, ioUtils);
    }

    /**
     * Analyzes all java classes found recursively in the given root directories for
     * matches of banned imports.
     *
     * @param roots The source directories.
     * @param group The banned import.
     * @return The result of analyzing the given source files.
     */
    AnalyzeResult analyze(Stream<Path> roots, BannedImportGroup group);
}
