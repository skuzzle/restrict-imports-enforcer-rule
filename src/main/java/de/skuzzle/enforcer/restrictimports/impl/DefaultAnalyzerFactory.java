package de.skuzzle.enforcer.restrictimports.impl;

import de.skuzzle.enforcer.restrictimports.api.AnalyzerFactory;
import de.skuzzle.enforcer.restrictimports.api.SourceTreeAnalyzer;

public final class DefaultAnalyzerFactory implements AnalyzerFactory {

    private static final AnalyzerFactory INSTANCE = new DefaultAnalyzerFactory();

    private DefaultAnalyzerFactory() {
        // hidden constructor
    }

    public static AnalyzerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public SourceTreeAnalyzer createAnalyzer() {
        final IOUtils ioUtils = new IOUtilsImpl();
        final ImportMatcher matcher = new ImportMatcherImpl(ioUtils::lines);
        return new SourceTreeAnalyzerImpl(matcher, ioUtils);
    }

}
