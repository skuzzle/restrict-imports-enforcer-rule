package de.skuzzle.enforcer.restrictimports.analyze;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

class IOUtils {

    /**
     * Lists files in the given root directory that match the given predicate. Returns an
     * empty stream if the root does not exist. Otherwise returns a stream of all files
     * found recursively within the root directory and all sub directories.
     *
     * @param root The root to search in.
     * @param filter For filtering the returned files.
     * @return A stream of found files.
     * @throws RuntimeIOException If an IO error occurs.
     */
    public Stream<Path> listFiles(Path root, Predicate<Path> filter) {
        try {
            if (!Files.exists(root)) {
                return Stream.empty();
            }
            return Files.find(root, Integer.MAX_VALUE, (path, bfa) -> filter.test(path));
        } catch (final IOException e) {
            throw new RuntimeIOException(
                    "Encountered IOException while listing files of " + root, e);
        }
    }

    /**
     * Determines whether the given path represents a file.
     *
     * @param path The path to check.
     * @return Whether the path represents a file.
     */
    public boolean isFile(Path path) {
        return !Files.isDirectory(path);
    }

}
