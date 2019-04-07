package de.skuzzle.enforcer.restrictimports.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface LineSupplier {
    Stream<String> lines(Path path) throws IOException;
}
