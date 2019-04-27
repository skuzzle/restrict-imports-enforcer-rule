package de.skuzzle.enforcer.restrictimports.analyze;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class SourceFileBuilder {

    private final FileSystem mockFileSystem;
    private Path file;
    private Charset charset = StandardCharsets.UTF_8;

    public SourceFileBuilder(FileSystem mockFileSystem) {
        this.mockFileSystem = mockFileSystem;
    }

    public SourceFileBuilder atPath(String first)
            throws IOException {

        final String[] parts = first.split("/");

        file = mockFileSystem.getPath(parts[0],
                Arrays.copyOfRange(parts, 1, parts.length));
        Files.createDirectories(file.getParent());
        return this;
    }

    public SourceFileBuilder witchCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public Path withLines(CharSequence... lines) throws IOException {
        final Iterable<? extends CharSequence> it = Arrays.stream(lines)::iterator;
        Files.write(file, it, charset);
        return file.toAbsolutePath();
    }
}