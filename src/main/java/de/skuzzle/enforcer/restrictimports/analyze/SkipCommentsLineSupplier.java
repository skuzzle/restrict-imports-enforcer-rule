package de.skuzzle.enforcer.restrictimports.analyze;

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
class SkipCommentsLineSupplier implements LineSupplier {

    private final Charset charset;
    private final int commentLineBufferSize;

    public SkipCommentsLineSupplier(Charset charset, int commentLineBufferSize) {
        this.charset = charset;
        this.commentLineBufferSize = commentLineBufferSize;
    }

    @Override
    public Stream<String> lines(Path path) throws IOException {
        final Reader fromFile = Files.newBufferedReader(path, charset);
        final Reader skipComments = new TransientCommentReader(fromFile,
                true,
                commentLineBufferSize);

        final BufferedReader lineReader = new BufferedReader(skipComments);
        return Stream.of(lineReader)
                .flatMap(this::readLine)
                .onClose(() -> close(lineReader));
    }

    private void close(Reader reader) {
        try {
            reader.close();
        } catch (final IOException e) {
            throw new RuntimeIOException("Error while closing reader", e);
        }
    }

    private Stream<String> readLine(BufferedReader reader) {
        try {
            final String line = reader.readLine();
            return line == null
                    ? Stream.empty()
                    : Stream.of(line)
                            .flatMap(l -> Stream.concat(Stream.of(l), readLine(reader)));
        } catch (final IOException e) {
            throw new RuntimeIOException("Error while reading next line", e);
        }
    }

}
