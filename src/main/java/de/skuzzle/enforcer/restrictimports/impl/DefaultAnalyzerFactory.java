package de.skuzzle.enforcer.restrictimports.impl;

import de.skuzzle.enforcer.restrictimports.AnalyzerFactory;
import de.skuzzle.enforcer.restrictimports.IOUtils;
import de.skuzzle.enforcer.restrictimports.ImportMatcher;
import de.skuzzle.enforcer.restrictimports.SourceTreeAnalyzer;

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
