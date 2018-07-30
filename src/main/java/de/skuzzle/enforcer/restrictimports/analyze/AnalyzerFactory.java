package de.skuzzle.enforcer.restrictimports.analyze;

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
        return AnalyzerFactoryImpl.getInstance();
    }

    /**
     * Creates a new {@link SourceTreeAnalyzer} instance.
     *
     * @return The analyzer.
     */
    SourceTreeAnalyzer createAnalyzer();
}
