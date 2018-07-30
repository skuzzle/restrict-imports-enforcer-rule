package de.skuzzle.enforcer.restrictimports.analyze;

final class AnalyzerFactoryImpl implements AnalyzerFactory {

    private static final AnalyzerFactory INSTANCE = new AnalyzerFactoryImpl();

    private AnalyzerFactoryImpl() {
        // hidden constructor
    }

    public static AnalyzerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public SourceTreeAnalyzer createAnalyzer() {
        final IOUtils ioUtils = new IOUtils();
        final ImportMatcher matcher = new ImportMatcherImpl(ioUtils::lines);
        return new SourceTreeAnalyzerImpl(matcher, ioUtils);
    }

}
