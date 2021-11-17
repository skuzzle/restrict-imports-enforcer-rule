package de.skuzzle.enforcer.restrictimports.analyze;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.skuzzle.enforcer.restrictimports.util.Preconditions;

/**
 * Pattern class to match java style package and class names using wild card operators.
 *
 * @author Simon Taddiken
 */
public final class PackagePattern implements Comparable<PackagePattern> {

    private static final String STATIC_PREFIX = "static ";
    private final String[] parts;
    private final boolean staticc;

    private PackagePattern(String s) {
        this.staticc = s.startsWith(STATIC_PREFIX);
        if (staticc) {
            s = s.substring(STATIC_PREFIX.length());
        }

        this.parts = s.split("\\.");
        checkParts(s, this.parts);
    }

    /**
     * Parses each string of the given collection into a {@link PackagePattern} and
     * returns them in a list.
     *
     * @param patternStrings The Strings to parse.
     * @return A list of parsed package patterns.
     */
    public static List<PackagePattern> parseAll(Collection<String> patternStrings) {
        Preconditions.checkArgument(patternStrings != null);
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
        Preconditions.checkArgument(patternString != null);
        return new PackagePattern(patternString);
    }

    private void checkParts(String full, String[] parts) {
        if (full.startsWith(".") || full.endsWith(".")) {
            throw new IllegalArgumentException(String.format("The pattern '%s' contains an empty part", full));
        }
        for (int i = 0; i < parts.length; i++) {
            final String part = parts[i];
            checkCharacters(full, part, i);
        }
    }

    private void checkCharacters(String full, String part, int partIndex) {
        final char[] chars = part.toCharArray();

        if (part.isEmpty()) {
            throw new IllegalArgumentException(String.format("The pattern '%s' contains an empty part", full));
        } else if ("*".equals(part) || "**".equals(part) || "'*'".equals(part)) {
            return;
        } else if (part.contains("*")) {
            throw new IllegalArgumentException(String.format(
                    "The pattern '%s' contains a part which mixes wildcards and normal characters", full));
        } else if (partIndex == 0 && "static".equals(part)) {
            return;
        } else if (!Character.isJavaIdentifierStart(chars[0])) {
            throw new IllegalArgumentException(String.format(
                    "The pattern '%s' contains a non-identifier character '%s'", full, chars[0]));
        }

        for (int i = 1; i < chars.length; i++) {
            final char c = chars[i];
            if (!Character.isJavaIdentifierPart(c)) {
                throw new IllegalArgumentException(String.format(
                        "The pattern '%s' contains a non-identifier character '%s'", full, chars[i]));
            }
        }
    }

    /**
     * Tests whether the given package pattern is matched by this package pattern
     * instance.
     *
     * @param otherPackagePattern The package pattern to match against this pattern.
     * @return Whether the pattern matches this pattern.
     * @since 0.8.0
     */
    public boolean matches(PackagePattern otherPackagePattern) {
        if (otherPackagePattern == this) {
            return true;
        }
        return matchesInternal(otherPackagePattern.staticc, otherPackagePattern.parts, this.staticc, this.parts);
    }

    /**
     * Tests whether the given package name is matched by this package pattern instance.
     *
     * @param packageName The package name to match against this pattern.
     * @return Whether the name matches this pattern.
     */
    public boolean matches(String packageName) {
        final boolean matchIsStatic = packageName.startsWith(STATIC_PREFIX);
        if (matchIsStatic) {
            packageName = packageName.substring(STATIC_PREFIX.length());
        }
        final String[] matchParts = packageName.split("\\.");
        return matchesInternal(matchIsStatic, matchParts, this.staticc, this.parts);
    }

    private boolean matchesInternal(boolean matchIsStatic, String[] matchParts,
            boolean partsIsStatic, String[] parts) {
        if (matchIsStatic != partsIsStatic) {
            return false;
        } else if (parts.length > matchParts.length) {
            // if the pattern is longer than the string to match, match cant be true
            return false;
        }

        int patternIndex = 0;
        int matchIndex = 0;
        for (; patternIndex < parts.length
                && matchIndex < matchParts.length; ++patternIndex) {
            final String patternPart = parts[patternIndex];
            final String matchPart = matchParts[matchIndex];

            if ("**".equals(patternPart)) {
                if (patternIndex + 1 < parts.length) {
                    final String nextPatternPart = parts[patternIndex + 1];
                    while (matchIndex < matchParts.length
                            && !matchParts(nextPatternPart, matchParts[matchIndex])) {
                        ++matchIndex;
                    }
                } else {
                    matchIndex = matchParts.length;
                }
            } else if (matchParts(patternPart, matchPart)) {
                ++matchIndex;
            } else {
                return false;
            }
        }

        return patternIndex == parts.length && matchIndex == matchParts.length;
    }

    private static boolean matchParts(String patternPart, String matchPart) {
        if ("*".equals(patternPart) || "**".equals(patternPart)) {
            return true;
        } else if ("'*'".equals(patternPart)) {
            return matchPart.equals("*");
        }
        return patternPart.equals(matchPart);
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        if (staticc) {
            result.append(STATIC_PREFIX);
        }
        result.append(String.join(".", this.parts));
        return result.toString();
    }

    @Override
    public int compareTo(PackagePattern other) {
        final int commonParts = Math.min(parts.length, other.parts.length);
        for (int i = 0; i < commonParts; ++i) {
            final String thisPart = this.parts[i];
            final String otherPart = other.parts[i];

            if (!thisPart.equals(otherPart)) {
                // mismatching parts, so we found a specificy difference
                final int leftSpecificy = specificyOf(thisPart);
                final int rightSpecificy = specificyOf(otherPart);
                return Integer.compare(leftSpecificy, rightSpecificy);
            }
        }
        // all parts are equal up to here, so the longer the more specific
        return Integer.compare(parts.length, other.parts.length);
    }

    private int specificyOf(String part) {
        if (part.equals("**")) {
            return 0;
        } else if (part.equals("*")) {
            return 1;
        }
        return 2;
    }

    /**
     * Whether this is a pattern starting with 'static '.
     *
     * @return Whether this is a static pattern.
     * @since 0.12.0
     */
    public boolean isStatic() {
        return staticc;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(parts), staticc);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof PackagePattern
                && this.staticc == ((PackagePattern) obj).staticc
                && Arrays.equals(this.parts, ((PackagePattern) obj).parts);
    }
}
