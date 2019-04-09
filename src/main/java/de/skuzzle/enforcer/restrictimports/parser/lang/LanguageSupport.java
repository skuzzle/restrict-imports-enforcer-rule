package de.skuzzle.enforcer.restrictimports.parser.lang;

import com.google.common.base.Preconditions;
import de.skuzzle.enforcer.restrictimports.parser.ImportStatement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * SPI for plugging in import statement recognition for different languages.
 * Implementations will be looked up using java's {@link ServiceLoader}.
 *
 * @author Simon Taddiken
 */
public interface LanguageSupport {

    /**
     * Looks up the available {@link LanguageSupport} implementations that can be
     * found using Java's {@link ServiceLoader}
     *
     * @return The implementations, mapped by their supported extensions.
     */
    static Map<String, LanguageSupport> lookupImplementations() {
        final ServiceLoader<LanguageSupport> serviceProvider = ServiceLoader.load(LanguageSupport.class);
        final Map<String, LanguageSupport> implementations = new HashMap<>();
        serviceProvider.forEach(parser -> parser.getSupportedFileExtensions().forEach(extension -> {
            final String normalizedExtension = determineNormalizedExtension(extension);

            if (implementations.put(normalizedExtension, parser) != null) {
                throw new IllegalStateException(
                        "There are multiple parsers to handle file extension: " + normalizedExtension);
            }
        }));
        Preconditions.checkState(!implementations.isEmpty(), "No LanguageSupport instances found!");
        return implementations;
    }

    /**
     * Returns the normalized extension that can be used to look up a {@link LanguageSupport} from the map returned by {@link #lookupImplementations()}.
     *
     * @param extension The extension to normalize.
     * @return The normalized extension.
     */
    static String determineNormalizedExtension(String extension) {
        return extension.startsWith(".")
                ? extension.toLowerCase()
                : "." + extension.toLowerCase();
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
