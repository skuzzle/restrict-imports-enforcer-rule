package de.skuzzle.enforcer.restrictimports.parser.lang;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

import de.skuzzle.enforcer.restrictimports.util.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to list available {@link LanguageSupport} implementations from
 * {@link ServiceLoader}.
 */
final class SupportedLanguageHolder {

    private static final Logger logger = LoggerFactory.getLogger(SupportedLanguageHolder.class);

    // maps from normalized extension to LanguageSupport instance
    // normalized extensions are all lower case and have a leading '.'
    static final Map<String, LanguageSupport> supportedLanguages = lookupImplementations();

    static Optional<LanguageSupport> getLanguageSupport(String extension) {
        final String normalizedExtension = determineNormalizedExtension(extension);
        return Optional.ofNullable(supportedLanguages.get(normalizedExtension));
    }

    static boolean isLanguageSupported(String extension) {
        final String normalizedExtension = determineNormalizedExtension(extension);
        return supportedLanguages.containsKey(normalizedExtension);
    }

    /**
     * Looks up the available {@link LanguageSupport} implementations that can be found
     * using Java's {@link ServiceLoader}
     *
     * @return The implementations, mapped by their supported extensions.
     */
    private static Map<String, LanguageSupport> lookupImplementations() {
        final ClassLoader classLoader = SupportedLanguageHolder.class.getClassLoader();
        final ServiceLoader<LanguageSupport> serviceProvider = ServiceLoader.load(LanguageSupport.class, classLoader);
        final Map<String, LanguageSupport> implementations = new HashMap<>();
        serviceProvider.forEach(parser -> parser.getSupportedFileExtensions().stream()
                .map(SupportedLanguageHolder::determineNormalizedExtension)
                .forEach(normalizedExtension -> {

                    if (implementations.put(normalizedExtension, parser) != null) {
                        throw new IllegalStateException(
                                "There are multiple parsers to handle file extension: " + normalizedExtension);
                    }
                    logger.debug("Registered {} for extension '{}'", parser, normalizedExtension);
                }));
        Preconditions.checkState(!implementations.isEmpty(), "No LanguageSupport instances found!");
        return implementations;
    }

    private static String determineNormalizedExtension(String extension) {
        return extension.startsWith(".")
                ? extension.toLowerCase()
                : "." + extension.toLowerCase();
    }
}
