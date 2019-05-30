package de.skuzzle.enforcer.restrictimports.analyze;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Final result of analyzing the code base for banned imports.
 */
public final class AnalyzeResult {

    private final List<MatchedFile> srcMatches;
    private final List<MatchedFile> testMatches;

    private AnalyzeResult(List<MatchedFile> srcMatches, List<MatchedFile> testMatches) {
        this.srcMatches = srcMatches;
        this.testMatches = testMatches;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Contains all the matches that were found within the analyzed compile
     * source files.
     *
     * @return The list of found banned imports.
     */
    public List<MatchedFile> getSrcMatches() {
        return this.srcMatches;
    }

    /**
     * Returns the matches that occurred in compile source files grouped by their {@link BannedImportGroup}
     *
     * @return The matches grouped by {@link BannedImportGroup}
     */
    public Map<BannedImportGroup, List<MatchedFile>> srcMatchesByGroup() {
        return srcMatches.stream()
                .collect(Collectors.groupingBy(MatchedFile::getMatchedBy));
    }

    /**
     * Contains all the matches that were found within the analyzed test source
     * files.
     *
     * @return The list of found banned imports.
     */
    public List<MatchedFile> getTestMatches() {
        return testMatches;
    }

    /**
     * Returns the matches that occurred in test source files grouped by their {@link BannedImportGroup}
     *
     * @return The matches grouped by {@link BannedImportGroup}
     */
    public Map<BannedImportGroup, List<MatchedFile>> testMatchesByGroup() {
        return testMatches.stream()
                .collect(Collectors.groupingBy(MatchedFile::getMatchedBy));
    }

    /**
     * Returns whether at least one banned import has been found within the
     * analyzed compile OR test source files.
     *
     * @return Whether a banned import has been found.
     */
    public boolean bannedImportsFound() {
        return bannedImportsInCompileCode() || bannedImportsInTestCode();
    }

    /**
     * Returns whether at least one banned import has been found within the
     * analyzed compile source code.
     *
     * @return Whether a banned import has been found.
     */
    public boolean bannedImportsInCompileCode() {
        return !srcMatches.isEmpty();
    }

    /**
     * Returns whether at least one banned import has been found within the
     * analyzed test source code.
     *
     * @return Whether a banned import has been found.
     */
    public boolean bannedImportsInTestCode() {
        return !testMatches.isEmpty();
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
        return MoreObjects.toStringHelper(this)
                .add("srcMatches", this.srcMatches)
                .add("testMatches", this.testMatches)
                .toString();
    }

    public static class Builder {
        private final List<MatchedFile> srcMatches = new ArrayList<>();
        private final List<MatchedFile> testMatches = new ArrayList<>();

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

        public AnalyzeResult build() {
            return new AnalyzeResult(srcMatches, testMatches);
        }
    }
}
