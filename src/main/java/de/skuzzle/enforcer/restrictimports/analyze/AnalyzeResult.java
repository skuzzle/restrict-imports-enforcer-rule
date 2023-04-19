package de.skuzzle.enforcer.restrictimports.analyze;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import de.skuzzle.enforcer.restrictimports.util.StringRepresentation;

/**
 * Final result of analyzing the code base for banned imports.
 */
public final class AnalyzeResult {

    private final List<MatchedFile> srcMatches;
    private final List<MatchedFile> testMatches;
    private final Duration duration;
    private final int analysedFiles;

    private AnalyzeResult(List<MatchedFile> srcMatches, List<MatchedFile> testMatches, long duration,
            int analysedFiles) {
        this.srcMatches = srcMatches;
        this.testMatches = testMatches;
        this.duration = Duration.ofMillis(duration);
        this.analysedFiles = analysedFiles;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Contains all the matches that were found within the analyzed compile source files.
     *
     * @return The list of found banned imports.
     */
    public List<MatchedFile> getSrcMatches() {
        return this.srcMatches;
    }

    /**
     * Returns the matches that occurred in compile source files grouped by their
     * {@link BannedImportGroup}
     *
     * @return The matches grouped by {@link BannedImportGroup}
     */
    public Map<BannedImportGroup, List<MatchedFile>> srcMatchesByGroup() {
        return srcMatches.stream()
                .filter(MatchedFile::hasBannedImports)
                .collect(Collectors.groupingBy(matchedFile -> matchedFile.getMatchedBy().get()));
    }

    /**
     * Contains all the matches that were found within the analyzed test source files.
     *
     * @return The list of found banned imports.
     */
    public List<MatchedFile> getTestMatches() {
        return testMatches;
    }

    /**
     * Returns the matches that occurred in test source files grouped by their
     * {@link BannedImportGroup}.
     *
     * @return The matches grouped by {@link BannedImportGroup}
     */
    public Map<BannedImportGroup, List<MatchedFile>> testMatchesByGroup() {
        return testMatches.stream()
                .filter(MatchedFile::hasBannedImports)
                .collect(Collectors.groupingBy(matchedFile -> matchedFile.getMatchedBy().get()));
    }

    /**
     * Returns wheter either a warning or a banned import has been found in any source
     * root
     *
     * @return Whether any reportable results where detected.
     * @since 2.2.0
     */
    public boolean bannedImportsOrWarningsFound() {
        return bannedImportsFound() || warningsFound();
    }

    /**
     * Returns whether at least one banned import has been found within the analyzed
     * compile OR test source files.
     *
     * @return Whether a banned import has been found.
     */
    public boolean bannedImportsFound() {
        return bannedImportsInCompileCode() || bannedImportsInTestCode();
    }

    /**
     * Returns whether at least one banned import has been found within the analyzed
     * compile source code.
     *
     * @return Whether a banned import has been found.
     */
    public boolean bannedImportsInCompileCode() {
        return bannedImportsFoundIn(srcMatches);
    }

    /**
     * Returns whether at least one banned import has been found within the analyzed test
     * source code.
     *
     * @return Whether a banned import has been found.
     */
    public boolean bannedImportsInTestCode() {
        return bannedImportsFoundIn(testMatches);
    }

    private boolean bannedImportsFoundIn(List<MatchedFile> matchedFiles) {
        return matchedFiles.stream().anyMatch(MatchedFile::hasBannedImports);
    }

    /**
     * @return Whether warnings were detected while analysing compile or code.
     * @since 2.2.0
     * @see #warningsFoundInCompileCode()
     * @see #warningsFoundInTestCode()
     */
    public boolean warningsFound() {
        return warningsFoundInCompileCode() || warningsFoundInTestCode();
    }

    /**
     * @return Whether warnings were detected while analysing compile code.
     * @since 2.2.0
     */
    public boolean warningsFoundInCompileCode() {
        return warningsFound(srcMatches);
    }

    /**
     * @return Whether warnings were detected while analysing test code.
     * @since 2.2.0
     */
    public boolean warningsFoundInTestCode() {
        return warningsFound(testMatches);
    }

    private boolean warningsFound(List<MatchedFile> matches) {
        return matches.stream().anyMatch(matchedFile -> !matchedFile.getWarnings().isEmpty());
    }

    /**
     * How long the analysis took, in ms.
     *
     * @return Analysis duration in ms.
     */
    public Duration duration() {
        return this.duration;
    }

    /**
     * The number of files that have been analysed.
     *
     * @return Number of files.
     */
    public int analysedFiles() {
        return this.analysedFiles;
    }

    @Override
    public int hashCode() {
        return Objects.hash(srcMatches, testMatches);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof AnalyzeResult
                && Objects.equals(srcMatches, ((AnalyzeResult) obj).srcMatches)
                && Objects.equals(testMatches, ((AnalyzeResult) obj).testMatches);
    }

    @Override
    public String toString() {
        return StringRepresentation.ofInstance(this)
                .add("srcMatches", this.srcMatches)
                .add("testMatches", this.testMatches)
                .add("duration", duration)
                .toString();
    }

    public static class Builder {
        private final List<MatchedFile> srcMatches = new ArrayList<>();
        private final List<MatchedFile> testMatches = new ArrayList<>();
        private long duration;
        private int analysedFiles;

        private Builder() {
            // hidden
        }

        public Builder withMatches(Collection<MatchedFile> matches) {
            this.srcMatches.addAll(matches);
            return this;
        }

        public Builder withMatches(MatchedFile.Builder... matches) {
            Arrays.stream(matches).map(MatchedFile.Builder::build)
                    .forEach(this.srcMatches::add);
            return this;
        }

        public Builder withMatchesInTestCode(Collection<MatchedFile> matches) {
            this.testMatches.addAll(matches);
            return this;
        }

        public Builder withMatchesInTestCode(MatchedFile.Builder... matches) {
            Arrays.stream(matches).map(MatchedFile.Builder::build)
                    .forEach(this.testMatches::add);
            return this;
        }

        public Builder withDuration(long duration) {
            this.duration = duration;
            return this;
        }

        public Builder withAnalysedFileCount(int analysedFiles) {
            this.analysedFiles = analysedFiles;
            return this;
        }

        public AnalyzeResult build() {
            return new AnalyzeResult(srcMatches, testMatches, duration, analysedFiles);
        }

    }
}
