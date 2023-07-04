package de.skuzzle.enforcer.restrictimports.formatting;

import java.time.Duration;

final class DurationFormat {

    private DurationFormat() {
        // hidden
    }

    public static String formatDuration(Duration duration) {
        if (duration.getSeconds() == 0) {
            return "less than 1 second";
        }
        final StringBuilder result = new StringBuilder();
        final long minutes = duration.toMinutes();
        if (minutes > 0) {
            result.append(pluralize(minutes, " minute"));
        }
        final long seconds = duration.getSeconds() % 60;
        if (seconds > 0) {
            if (result.length() != 0) {
                result.append(" and ");
            }
            result.append(pluralize(seconds, " second"));
        }
        return result.toString();
    }

    private static String pluralize(long value, String singular) {
        return value == 1
                ? value + singular
                : value + singular + "s";
    }
}
