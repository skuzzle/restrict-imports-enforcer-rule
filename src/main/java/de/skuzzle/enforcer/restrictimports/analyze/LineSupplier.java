package de.skuzzle.enforcer.restrictimports.analyze;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

interface LineSupplier {
    Stream<String> lines(Path path) throws IOException;
}
