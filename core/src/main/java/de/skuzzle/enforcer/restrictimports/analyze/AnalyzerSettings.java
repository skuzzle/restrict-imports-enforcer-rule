package de.skuzzle.enforcer.restrictimports.analyze;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import de.skuzzle.enforcer.restrictimports.util.StringRepresentation;

/**
 * Defines context information for the {@link SourceTreeAnalyzer}.
 *
 * @author Simon Taddiken
 */
public final class AnalyzerSettings {

    private final Charset sourceFileCharset;
    private final Collection<Path> srcDirectories;
    private final Collection<Path> testDirectories;
    private final boolean parallel;
    private final boolean parseFullCompilationUnit;

    private AnalyzerSettings(Charset sourceFileCharset,
            Collection<Path> srcDirectories,
            Collection<Path> testDirectories, boolean parallel, boolean parseFullCompilationUnit) {
        this.sourceFileCharset = sourceFileCharset;
        this.srcDirectories = srcDirectories;
        this.testDirectories = testDirectories;
        this.parallel = parallel;
        this.parseFullCompilationUnit = parseFullCompilationUnit;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Charset getSourceFileCharset() {
        return this.sourceFileCharset;
    }

    public Collection<Path> getSrcDirectories() {
        return this.srcDirectories;
    }

    public Collection<Path> getTestDirectories() {
        return testDirectories;
    }

    public boolean isParallel() {
        return this.parallel;
    }

    /**
     * When supported for a specific source file, we will attempt to parse the full source
     * file. This allows for a more accurate detection of imports.
     *
     * @return Whether to attempt a full parse.
     * @since 2.1.0
     */
    public boolean isParseFullCompilationUnit() {
        return this.parseFullCompilationUnit;
    }

    /**
     * Returns the union of {@link #getSrcDirectories()} and getTestDirectories.
     *
     * @return All source directories that are subject to analysis.
     */
    public Collection<Path> getAllDirectories() {
        final Set<Path> result = new HashSet<>(srcDirectories.size() + testDirectories.size());
        result.addAll(srcDirectories);
        result.addAll(testDirectories);
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceFileCharset, srcDirectories, testDirectories, parallel, parseFullCompilationUnit);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof AnalyzerSettings
                && Objects.equals(sourceFileCharset, ((AnalyzerSettings) obj).sourceFileCharset)
                && Objects.equals(srcDirectories, ((AnalyzerSettings) obj).srcDirectories)
                && Objects.equals(testDirectories, ((AnalyzerSettings) obj).testDirectories)
                && parallel == ((AnalyzerSettings) obj).parallel
                && parseFullCompilationUnit == ((AnalyzerSettings) obj).parseFullCompilationUnit;
    }

    @Override
    public String toString() {
        return StringRepresentation.ofInstance(this)
                .add("sourceFileCharset", sourceFileCharset)
                .add("srcDirectories", srcDirectories)
                .add("testDirectories", testDirectories)
                .add("parallel", parallel)
                .add("parseFullCompilationUnit", parseFullCompilationUnit)
                .toString();
    }

    public static final class Builder {

        private final List<Path> srcDirectories = new ArrayList<>();
        private final List<Path> testDirectories = new ArrayList<>();
        private Charset sourceFileCharset = Charset.defaultCharset();
        private boolean parallel = false;
        private boolean parseFullCompilationUnit = false;

        private Builder() {
            // hidden
        }

        public Builder withSrcDirectories(Collection<Path> srcDirectories) {
            this.srcDirectories.addAll(srcDirectories);
            return this;
        }

        public Builder withSrcDirectories(Path... srcDirectories) {
            this.srcDirectories.addAll(Arrays.asList(srcDirectories));
            return this;
        }

        public Builder withTestDirectories(Collection<Path> testDirectories) {
            this.testDirectories.addAll(testDirectories);
            return this;
        }

        public Builder withTestDirectories(Path... testDirectories) {
            this.testDirectories.addAll(Arrays.asList(testDirectories));
            return this;
        }

        public Builder withSourceFileCharset(Charset sourceFileCharset) {
            this.sourceFileCharset = sourceFileCharset;
            return this;
        }

        public Builder enableParallelAnalysis(boolean parallel) {
            this.parallel = parallel;
            return this;
        }

        public Builder withParseFullCompilationUnit(boolean parseFullCompilationUnit) {
            this.parseFullCompilationUnit = parseFullCompilationUnit;
            return this;
        }

        public AnalyzerSettings build() {
            return new AnalyzerSettings(sourceFileCharset, srcDirectories, testDirectories, parallel,
                    parseFullCompilationUnit);
        }
    }
}
