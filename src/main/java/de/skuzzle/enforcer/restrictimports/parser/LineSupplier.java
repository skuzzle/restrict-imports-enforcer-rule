package de.skuzzle.enforcer.restrictimports.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Internal interface for reading a file line by line.
 */
interface LineSupplier {
    Stream<String> lines(Path path) throws IOException;
}
