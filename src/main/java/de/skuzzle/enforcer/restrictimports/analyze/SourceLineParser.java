package de.skuzzle.enforcer.restrictimports.analyze;

import java.util.List;
import java.util.Optional;

interface SourceLineParser {

    /**
     * Parses the given line and returns the declared package
     *
     * @param line Line in the source file
     * @return The package declared
     */
    Optional<String> parsePackage(String line);

    /**
     * Extract the package names that this import statement represents. As some languages
     * allow to specify multiple imports in a single line, this method returns a list.
     *
     * e.g. import java.util.List; The above should return java.util.List in a Java source
     * file.
     *
     * @param importLine Line in the source file
     * @return Fully qualified package name that this import represents
     */
    List<String> parseImport(String importLine);
}
