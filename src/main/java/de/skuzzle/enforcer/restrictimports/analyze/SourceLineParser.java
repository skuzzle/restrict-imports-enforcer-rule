package de.skuzzle.enforcer.restrictimports.analyze;

import java.util.Optional;

public interface SourceLineParser {

    /**
     * Parses the given line and returns the declared package
     * @param line Line in the source file
     * @return The package declared
     */
    Optional<String> parsePackage(String line);


    /**
     * Extract the package name that this import represents.
     *
     * e.g. import java.util.List;
     * The above should return java.util.List in a Java source file
     *
     * @param line Line in the source file
     * @return Fully qualified package name that this import represents
     */
    Optional<String> parseImport(String line);
}
