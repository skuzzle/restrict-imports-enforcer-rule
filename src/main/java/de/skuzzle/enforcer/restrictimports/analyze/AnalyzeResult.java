package de.skuzzle.enforcer.restrictimports.analyze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

public final class AnalyzeResult {

    private final List<MatchedFile> fileMatches;

    AnalyzeResult(List<MatchedFile> matches) {
        this.fileMatches = ImmutableList.copyOf(matches);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Contains all the matches that were found within the analyzed java source files.
     *
     * @return The list of found banned imports.
     */
    public List<MatchedFile> getFileMatches() {
        return this.fileMatches;
    }

    /**
     * Returns whether at least one banned import has been found within the analyzed java
     * source files.
     *
     * @return Whether a banned import has been found.
     */
    public boolean bannedImportsFound() {
        return !fileMatches.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileMatches);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof AnalyzeResult
                && Objects.equals(fileMatches, ((AnalyzeResult) obj).fileMatches);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("fileMatches", this.fileMatches)
                .toString();
    }

    public static class Builder {
        private final List<MatchedFile> matches = new ArrayList<>();

        private Builder() {
            // hidden
        }

        public Builder withMatches(MatchedFile.Builder... matches) {
            Arrays.stream(matches).map(MatchedFile.Builder::build)
                    .forEach(this.matches::add);
            return this;
        }

        public AnalyzeResult build() {
            return new AnalyzeResult(matches);
        }
    }
}
