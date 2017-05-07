package de.skuzzle.enforcer.restrictimports.api;

import de.skuzzle.enforcer.restrictimports.impl.DefaultAnalyzerFactory;

/**
 * Factory class for obtaining {@link SourceTreeAnalyzer} instances.
 *
 * @author Simon Taddiken
 */
public interface AnalyzerFactory {

    /**
     * Gets an instance of the factory.
     *
     * @return The factory instance.
     */
    public static AnalyzerFactory getInstance() {
        return DefaultAnalyzerFactory.getInstance();
    }

    /**
     * Creates a new {@link SourceTreeAnalyzer} instance.
     *
     * @return The analyzer.
     */
    SourceTreeAnalyzer createAnalyzer();
}
