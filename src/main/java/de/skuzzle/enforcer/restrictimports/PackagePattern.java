package de.skuzzle.enforcer.restrictimports;

import static com.google.common.base.Preconditions.checkArgument;

interface PackagePattern {

    public static PackagePattern parse(String s) {
        checkArgument(s != null);
        return new PackagePatternImpl(s);
    }

    boolean matches(String packageName);
}
