package de.skuzzle.enforcer.restrictimports;

import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.skuzzle.enforcer.restrictimports.impl.RuntimeIOException;

/**
 * Some IO utility methods for java NIO features.
 *
 * @author Simon Taddiken
 */
public interface IOUtils {

    Stream<String> lines(Path path) throws RuntimeIOException;

    Stream<Path> listFiles(Path root, Predicate<Path> filter) throws RuntimeIOException;

    boolean isFile(Path path);
}
