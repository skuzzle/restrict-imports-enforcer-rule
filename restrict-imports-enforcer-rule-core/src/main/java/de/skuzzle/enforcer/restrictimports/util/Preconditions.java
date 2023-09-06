package de.skuzzle.enforcer.restrictimports.util;

public final class Preconditions {

    public static void checkArgument(boolean condition) {
        checkArgument(condition, "Unexpected argument");
    }

    public static void checkArgument(boolean condition, String message, Object... args) {
        if (!condition) {
            throw new IllegalArgumentException(String.format(message, args));
        }
    }

    public static void checkState(boolean condition) {
        checkState(condition, "Unexpected state");
    }

    public static void checkState(boolean condition, String message, Object... args) {
        if (!condition) {
            throw new IllegalStateException(String.format(message, args));
        }
    }

    private Preconditions() {
        throw new IllegalStateException("hidden");
    }
}
