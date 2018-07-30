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
public interface PackagePattern {

    /**
     * Parses each string of the given collection into a {@link PackagePattern} and
     * returns them in a list.
     *
     * @param strings The Strings to parse.
     * @return A list of parsed package patterns.
     */
    public static List<PackagePattern> parseAll(Collection<String> strings) {
        checkArgument(strings != null);
        return strings.stream()
                .map(PackagePattern::parse)
                .collect(Collectors.toList());
    }

    /**
     * Parses the given String into a {@link PackagePattern}.
     *
     * @param s The String to parse.
     * @return The parsed package pattern.
     */
    public static PackagePattern parse(String s) {
        checkArgument(s != null);
        return new PackagePatternImpl(s);
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
}
