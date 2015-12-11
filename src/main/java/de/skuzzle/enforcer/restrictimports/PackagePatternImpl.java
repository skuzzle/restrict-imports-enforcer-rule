package de.skuzzle.enforcer.restrictimports;

import edu.emory.mathcs.backport.java.util.Arrays;

final class PackagePatternImpl implements PackagePattern {

    private final String[] parts;

    PackagePatternImpl(String s) {
        this.parts = s.split("\\.");
        for (int i = 0; i < this.parts.length; ++i) {
            final boolean last = i == this.parts.length - 1;
            if (!last && "**".equals(this.parts[i])) {
                throw new IllegalArgumentException(
                        "Double wildcard '**' only allowed at end of pattern");
            }
        }
    }

    @Override
    public boolean matches(String packageName) {
        final String[] matchParts = packageName.split("\\.");
        final int count = Math.min(matchParts.length, this.parts.length);
        int i = 0;
        for (; i < count; ++i) {
            final String patternPart = this.parts[i];
            final String matchPart = matchParts[i];
            if (!matchParts(patternPart, matchPart)) {
                return false;
            }
        }
        if (this.parts.length == matchParts.length) {
            return true;
        } else if (this.parts.length > matchParts.length) {
            return false;
        } else {
            return "**".equals(this.parts[this.parts.length - 1]);
        }
    }

    private boolean matchParts(String patternPart, String matchPart) {
        if ("*".equals(patternPart) || "**".equals(patternPart)) {
            return true;
        }
        return patternPart.equals(matchPart);
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
