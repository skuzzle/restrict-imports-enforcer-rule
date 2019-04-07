package de.skuzzle.enforcer.restrictimports.parser;

import de.skuzzle.enforcer.restrictimports.analyze.RuntimeIOException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Supplies lines but skips every encountered comment. Block comments that span multiple
 * lines will be replaced by the same amount of empty lines.
 *
 * @author Simon Taddiken
 */
public class SkipCommentsLineSupplier implements LineSupplier {

    private final Charset charset;

    public SkipCommentsLineSupplier(Charset charset) {
        this.charset = charset;
    }

    @Override
    public Stream<String> lines(Path path) throws IOException {
        final Reader fromFile = Files.newBufferedReader(path, charset);
        final Reader skipComments = new TransientCommentReader(fromFile, true);

        final BufferedReader lineReader = new BufferedReader(skipComments);
        return lineReader.lines().onClose(() -> close(lineReader));
    }

    private void close(Reader reader) {
        try {
            reader.close();
        } catch (final IOException e) {
            throw new RuntimeIOException("Error while closing reader", e);
        }
    }
}
