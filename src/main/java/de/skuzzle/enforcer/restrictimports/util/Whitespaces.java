package de.skuzzle.enforcer.restrictimports.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for handling whitespaces.
 *
 * @author Simon Taddiken
 * @since 2.0.0
 */
public final class Whitespaces {

    private static final Pattern LEADING = Pattern.compile("^\\s+");
    private static final Pattern TRAILING = Pattern.compile("\\s+$");

    /**
     * Trims all leading and trailing whitespaces (regex pattern class <code>\s</code>)
     *
     * @param s The string to trim.
     * @return The trimmed string.
     */
    public static final String trimAll(String s) {
        final Matcher leadingMatcher = LEADING.matcher(s);
        final Matcher trailingMatcher = TRAILING.matcher(s);
        final boolean hasLeading = leadingMatcher.find();
        final boolean hasTrailing = trailingMatcher.find();
        if (!hasLeading && !hasTrailing) {
            return s;
        }
        final int start = hasLeading ? leadingMatcher.end() : 0;
        final int end = hasTrailing ? trailingMatcher.start() : s.length();
        return s.substring(start, end);
    }

    private Whitespaces() {
        // hidden
    }
}
