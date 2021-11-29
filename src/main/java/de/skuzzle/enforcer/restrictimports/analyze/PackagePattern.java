package de.skuzzle.enforcer.restrictimports.analyze;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.skuzzle.enforcer.restrictimports.util.Preconditions;
import de.skuzzle.enforcer.restrictimports.util.Whitespaces;

/**
 * Pattern class to match java style package and class names using wild card operators.
 *
 * @author Simon Taddiken
 */
public final class PackagePattern implements Comparable<PackagePattern> {

    private static final String STATIC_PREFIX = "static";

    private final String[] parts;
    private final boolean staticc;
    private final boolean ignoreStatic;

    private PackagePattern(String s, boolean ignoreStatic) {
        final ParseResult parsed = ParseResult.parse(s);
        this.ignoreStatic = ignoreStatic;
        this.staticc = parsed.staticc;
        this.parts = parsed.parts;
        checkParts(s, this.parts);
    }

    private static class ParseResult {
        private static final Pattern STATIC_PREFIX_PATTERN = Pattern.compile("^" + STATIC_PREFIX + "\\s+");

        private final String[] parts;
        private final boolean staticc;

        private ParseResult(String[] parts, boolean staticc) {
            this.parts = parts;
            this.staticc = staticc;
        }

        static ParseResult parse(String s) {
            String trimmed = Whitespaces.trimAll(s);
            final Matcher matcher = STATIC_PREFIX_PATTERN.matcher(trimmed);
            final boolean staticc = matcher.find();
            if (staticc) {
                trimmed = trimmed.substring(matcher.end());
            }
            final String[] parts = trimmed.split("\\.");
            return new ParseResult(parts, staticc);
        }
    }

    private void checkParts(String original, String[] parts) {
        if (original.startsWith(".") || original.endsWith(".")) {
            throw new IllegalArgumentException(String.format("The pattern '%s' contains an empty part", original));
        }
        for (int i = 0; i < parts.length; i++) {
            final String part = parts[i];
            checkCharacters(original, part, i);
        }
    }

    private void checkCharacters(String original, String part, int partIndex) {
        final char[] chars = part.toCharArray();

        if (part.isEmpty()) {
            throw new IllegalArgumentException(String.format("The pattern '%s' contains an empty part", original));
        } else if ("*".equals(part) || "**".equals(part) || "'*'".equals(part)) {
            return;
        } else if (part.contains("*")) {
            throw new IllegalArgumentException(String.format(
                    "The pattern '%s' contains a part which mixes wildcards and normal characters", original));
        } else if (partIndex == 0 && "static".equals(part)) {
            return;
        } else if (!Character.isJavaIdentifierStart(chars[0])) {
            throw new IllegalArgumentException(String.format(
                    "The pattern '%s' contains a non-identifier character '%s' (0x%s)", original, chars[0],
                    Integer.toHexString(chars[0])));
        }

        for (int i = 1; i < chars.length; i++) {
            final char c = chars[i];
            if (!Character.isJavaIdentifierPart(c)) {
                throw new IllegalArgumentException(String.format(
                        "The pattern '%s' contains a non-identifier character '%s' (0x%s)", original, chars[i],
                        Integer.toHexString(chars[i])));
            }
        }
    }

    /**
     * Parses each string of the given collection into a {@link PackagePattern} and
     * returns them in a list.
     *
     * @param patternStrings The Strings to parse.
     * @return A list of parsed package patterns.
     * @deprecated Use {@link #parseAll(Collection, boolean)} instead.
     */
    @Deprecated
    public static List<PackagePattern> parseAll(Collection<String> patternStrings) {
        return parseAll(patternStrings, false);
    }

    /**
     * Parses each string of the given collection into a {@link PackagePattern} and
     * returns them in a list.
     *
     * @param patternStrings The Strings to parse.
     * @param ignoreStatic If true, the parsed patterns match both static and non-static
     *            import strings.
     * @return A list of parsed package patterns.
     */
    public static List<PackagePattern> parseAll(Collection<String> patternStrings, boolean ignoreStatic) {
        Preconditions.checkArgument(patternStrings != null);
        return patternStrings.stream()
                .map(pattern -> parse(pattern, ignoreStatic))
                .collect(Collectors.toList());
    }

    /**
     * Parses the given String into a {@link PackagePattern}.
     *
     * @param patternString The String to parse.
     * @return The parsed package pattern.
     * @deprecated Use {@link #parse(String, boolean)} instead.
     */
    @Deprecated
    public static PackagePattern parse(String patternString) {
        return parse(patternString, false);
    }

    /**
     * Parses the given String into a {@link PackagePattern}.
     *
     * @param patternString The String to parse.
     * @param ignoreStatic If true, this pattern matches both static and non-static import
     *            strings.
     * @return The parsed package pattern.
     */
    public static PackagePattern parse(String patternString, boolean ignoreStatic) {
        Preconditions.checkArgument(patternString != null);
        return new PackagePattern(patternString, ignoreStatic);
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
        return matchesInternal(otherPackagePattern.staticc, otherPackagePattern.parts,
                this.staticc, this.parts, this.ignoreStatic);
    }

    /**
     * Tests whether the given package name is matched by this package pattern instance.
     *
     * @param packageName The package name to match against this pattern.
     * @return Whether the name matches this pattern.
     */
    public boolean matches(String packageName) {
        final ParseResult parsed = ParseResult.parse(packageName);
        return matchesInternal(parsed.staticc, parsed.parts, this.staticc, this.parts, this.ignoreStatic);
    }

    private boolean matchesInternal(boolean matchIsStatic, String[] matchParts,
            boolean patternIsStatic, String[] patternParts, boolean ignoreStatic) {
        if (!ignoreStatic && matchIsStatic != patternIsStatic) {
            return false;
        } else if (patternParts.length > matchParts.length) {
            // if the pattern is longer than the string to match, match cant be true
            return false;
        }

        int patternIndex = 0;
        int matchIndex = 0;
        for (; patternIndex < patternParts.length
                && matchIndex < matchParts.length; ++patternIndex) {
            final String patternPart = patternParts[patternIndex];
            final String matchPart = matchParts[matchIndex];

            if ("**".equals(patternPart)) {
                if (patternIndex + 1 < patternParts.length) {
                    final String nextPatternPart = patternParts[patternIndex + 1];
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

        return patternIndex == patternParts.length && matchIndex == matchParts.length;
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
            result.append(STATIC_PREFIX + " ");
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
        return Objects.hash(Arrays.hashCode(parts), staticc, ignoreStatic);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof PackagePattern
                && this.staticc == ((PackagePattern) obj).staticc
                && this.ignoreStatic == ((PackagePattern) obj).ignoreStatic
                && Arrays.equals(this.parts, ((PackagePattern) obj).parts);
    }
}
