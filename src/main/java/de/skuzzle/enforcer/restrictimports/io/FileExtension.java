package de.skuzzle.enforcer.restrictimports.io;

import java.nio.file.Path;

/**
 * Small utility for working with file extensions.
 *
 * @author Simon Taddiken
 * @since 1.1.0
 */
public final class FileExtension {

    private FileExtension() {
        // hidden
    }

    /**
     * Determines the extension from the file pointed to by the given path. If the file
     * has no extension (iff its name contains no '.') an empty String is returned.
     * <p>
     * There are no checks employed regarding whether the path actually points to a file.
     *
     * @param path The path to determine the file name from.
     * @return The file's extension including the leading '.', never null.
     */
    public static String fromPath(Path path) {
        final String fileName = path.getFileName().toString();
        final int index = fileName.lastIndexOf(".");

        if (index == -1) {
            return "";
        }

        return fileName.substring(index);
    }
}
