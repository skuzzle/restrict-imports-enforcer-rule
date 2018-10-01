package de.skuzzle.enforcer.restrictimports.analyze;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Pattern class to match java style package and class names using wild card operators.
 *
 * @author Simon Taddiken
 */
public interface PackagePattern extends Comparable<PackagePattern> {

    /**
     * Parses each string of the given collection into a {@link PackagePattern} and
     * returns them in a list.
     *
     * @param patternStrings The Strings to parse.
     * @return A list of parsed package patterns.
     */
    public static List<PackagePattern> parseAll(Collection<String> patternStrings) {
        checkArgument(patternStrings != null);
        return patternStrings.stream()
                .map(PackagePattern::parse)
                .collect(Collectors.toList());
    }

    /**
     * Parses the given String into a {@link PackagePattern}.
     *
     * @param patternString The String to parse.
     * @return The parsed package pattern.
     */
    public static PackagePattern parse(String patternString) {
        checkArgument(patternString != null);
        return new PackagePatternImpl(patternString);
    }

    /**
     * Tests whether the given package name is matched by this package pattern instance.
     *
     * @param packageName The package name to match against this pattern.
     * @return Whether the name matches this pattern.
     */
    boolean matches(String packageName);

    /**
     * Tests whether the given package pattern is matched by this package pattern
     * instance.
     *
     * @param packagePattern The package pattern to match against this pattern.
     * @return Whether the pattern matches this pattern.
     * @since 0.8.0
     */
    boolean matches(PackagePattern packagePattern);

    /**
     * Whether this is a pattern starting with 'static '.
     *
     * @return Whether this is a static pattern.
     * @since 0.12.0
     */
    boolean isStatic();
}
