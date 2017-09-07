package de.skuzzle.enforcer.restrictimports.impl;

import java.util.Arrays;
import java.util.stream.Collectors;

import de.skuzzle.enforcer.restrictimports.model.PackagePattern;

public final class PackagePatternImpl implements PackagePattern {
    private final String[] parts;

    public PackagePatternImpl(String s) {
        this.parts = s.split("\\.");
    }

    @Override
    public boolean matches(PackagePattern packagePattern) {
        if (packagePattern == this) {
            return true;
        } else if (packagePattern instanceof PackagePatternImpl) {
            final PackagePatternImpl ppi = (PackagePatternImpl) packagePattern;
            return matchesInternal(ppi.parts, this.parts);
        }
        return matches(packagePattern.toString());
    }

    @Override
    public boolean matches(String packageName) {
        final String[] matchParts = packageName.split("\\.");
        return matchesInternal(matchParts, this.parts);
    }

    private boolean matchesInternal(String[] matchParts, String[] parts) {
        if (parts.length > matchParts.length) {
            // if the pattern is longer than the string to match, match cant be true
            return false;
        }

        int patternIndex = 0;
        int matchIndex = 0;
        for (; patternIndex < parts.length
                && matchIndex < matchParts.length; ++patternIndex) {
            final String patternPart = this.parts[patternIndex];
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
        }
        return patternPart.equals(matchPart);
    }

    @Override
    public String toString() {
        return Arrays.stream(this.parts).collect(Collectors.joining("."));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.parts);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof PackagePatternImpl &&
                Arrays.equals(this.parts, ((PackagePatternImpl) obj).parts);
    }
}
