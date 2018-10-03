package de.skuzzle.enforcer.restrictimports.analyze;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * Defines context information for the {@link SourceTreeAnalyzer}.
 *
 * @author Simon Taddiken
 */
public final class AnalyzerSettings {

    private final Charset sourceFileCharset;
    private final Collection<Path> rootDirectories;
    private final int commentLineBufferSize;

    private AnalyzerSettings(Charset sourceFileCharset,
            Collection<Path> rootDirectories,
            int commentLineBufferSize) {
        this.sourceFileCharset = sourceFileCharset;
        this.rootDirectories = rootDirectories;
        this.commentLineBufferSize = commentLineBufferSize;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Charset getSourceFileCharset() {
        return this.sourceFileCharset;
    }

    public Collection<Path> getRootDirectories() {
        return this.rootDirectories;
    }

    public int getCommentLineBufferSize() {
        return this.commentLineBufferSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceFileCharset, rootDirectories, commentLineBufferSize);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof AnalyzerSettings
                && Objects.equals(sourceFileCharset, ((AnalyzerSettings) obj).sourceFileCharset)
                && Objects.equals(rootDirectories, ((AnalyzerSettings) obj).rootDirectories)
                && Objects.equals(commentLineBufferSize, ((AnalyzerSettings) obj).commentLineBufferSize);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("sourceFileCharset", sourceFileCharset)
                .add("commentLineBufferSize", commentLineBufferSize)
                .add("rootDirectories", rootDirectories)
                .toString();
    }

    public static final class Builder {

        private final List<Path> rootDirectories = new ArrayList<>();
        private Charset sourceFileCharset = Charset.defaultCharset();
        private int commentLineBufferSize = 128;

        private Builder() {
            // hidden
        }

        public Builder withRootDirectories(Collection<Path> rootDirectories) {
            this.rootDirectories.addAll(rootDirectories);
            return this;
        }

        public Builder withRootDirectories(Path... rootDirectories) {
            this.rootDirectories.addAll(Arrays.asList(rootDirectories));
            return this;
        }

        public Builder withSourceFileCharset(Charset sourceFileCharset) {
            this.sourceFileCharset = sourceFileCharset;
            return this;
        }

        public Builder withCommentLineBufferSize(int commentLineBufferSize) {
            this.commentLineBufferSize = commentLineBufferSize;
            return this;
        }

        public AnalyzerSettings build() {
            return new AnalyzerSettings(sourceFileCharset, rootDirectories,
                    commentLineBufferSize);
        }
    }
}
