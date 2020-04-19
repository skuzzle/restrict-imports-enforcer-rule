package de.skuzzle.enforcer.restrictimports.parser.lang;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

import de.skuzzle.enforcer.restrictimports.io.FileExtension;
import de.skuzzle.enforcer.restrictimports.parser.ImportStatement;

/**
 * SPI for plugging in import statement recognition for different languages.
 * Implementations will be looked up using java's {@link ServiceLoader}.
 *
 * @author Simon Taddiken
 */
public interface LanguageSupport {

    /**
     * Returns the {@link LanguageSupport} implementation for the given file extension.
     *
     * @param extension The extension.
     * @return The {@link LanguageSupport} implementation or an empty optional if none was
     *         found.
     */
    static Optional<LanguageSupport> getLanguageSupport(String extension) {
        return SupportedLanguageHolder.getLanguageSupport(extension);
    }

    /**
     * Determines whether there exists a {@link LanguageSupport} implementation for the
     * given extension.
     *
     * @param extension The extension.
     * @return Whether such implementation exists.
     */
    static boolean isLanguageSupported(String extension) {
        return SupportedLanguageHolder.isLanguageSupported(extension);
    }

    /**
     * Determines whether there exists a {@link LanguageSupport} implementation for the
     * given path.
     *
     * @param path The path to a file.
     * @return Whether such implementation exists.
     * @since 1.1.0
     */
    static boolean isLanguageSupported(Path path) {
        return !Files.isDirectory(path) && isLanguageSupported(FileExtension.fromPath(path));
    }

    /**
     * The set of supported file extensions. Extensions returned here are case insensitive
     * and may or may not start with a '.' (dot).
     *
     * @return The set of file extensions.
     */
    Set<String> getSupportedFileExtensions();

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
     * <p>
     * e.g. import java.util.List; The above should return java.util.List in a Java source
     * file.
     *
     * @param importLine Line in the source file
     * @param lineNumber The line number of the import.
     * @return Fully qualified package name that this import represents
     */
    List<ImportStatement> parseImport(String importLine, int lineNumber);
}
