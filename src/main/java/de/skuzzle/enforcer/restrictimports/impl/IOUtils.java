package de.skuzzle.enforcer.restrictimports.impl;

import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.skuzzle.enforcer.restrictimports.api.RuntimeIOException;

/**
 * Some IO utility methods for java NIO features. Only for better testability.
 *
 * @author Simon Taddiken
 */
interface IOUtils {

    /**
     * Returns a Stream of the given file's lines.
     *
     * @param path The file to read from.
     * @return A Stream of lines.
     * @throws RuntimeIOException If an IO error occurs.
     */
    Stream<String> lines(Path path);

    /**
     * Lists files in the given root directory that match the given predicate. Returns an
     * empty stream if the root does not exists. Otherwise returns a stream of all files
     * found within the root directory and all sub directories.
     *
     * @param root The root to search in.
     * @param filter For filtering the returned files.
     * @return A stream of found files.
     * @throws RuntimeIOException If an IO error occurs.
     */
    Stream<Path> listFiles(Path root, Predicate<Path> filter);

    /**
     * Determines whether the given path represents a file.
     *
     * @param path The path to check.
     * @return Whether the path represents a file.
     */
    boolean isFile(Path path);
}
