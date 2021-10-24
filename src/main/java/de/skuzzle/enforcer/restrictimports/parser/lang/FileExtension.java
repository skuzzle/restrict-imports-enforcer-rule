package de.skuzzle.enforcer.restrictimports.parser.lang;

import java.io.File;
import java.nio.file.Path;

final class FileExtension {

    static String fromFilename(String fullName) {
        final String fileName = new File(fullName).getName();
        final int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    static String fromPath(Path path) {
        return fromFilename(path.getFileName().toString());
    }

    private FileExtension() {
        throw new IllegalStateException("hidden");
    }
}
