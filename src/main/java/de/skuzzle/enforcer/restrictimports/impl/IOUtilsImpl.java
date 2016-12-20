package de.skuzzle.enforcer.restrictimports.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.skuzzle.enforcer.restrictimports.IOUtils;

public class IOUtilsImpl implements IOUtils {

    @Override
    public Stream<String> lines(Path path) throws RuntimeIOException {
        try {
            return Files.lines(path);
        } catch (final IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    @Override
    public Stream<Path> listFiles(Path root, Predicate<Path> filter) {
        try {
            if (!Files.exists(root)) {
                return Stream.empty();
            }
            return Files.find(root, Integer.MAX_VALUE, (path, bfa) -> filter.test(path));
        } catch (final IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    @Override
    public boolean isFile(Path path) {
        return !Files.isDirectory(path);
    }

}
