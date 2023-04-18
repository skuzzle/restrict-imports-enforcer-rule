package de.skuzzle.enforcer.restrictimports.analyze;

import java.util.Objects;

import de.skuzzle.enforcer.restrictimports.util.StringRepresentation;

/**
 * Represents an additional warning that might be reported and shall be displayed to the
 * end user.
 * <p>
 * Presence of a Waning alone on a MatchedFile usually doesn't fail the build as long as
 * no banned imports were detected.
 *
 * @since 2.2.0
 */
public final class Warning {

    private final String message;

    private Warning(String message) {
        this.message = message;
    }

    public static Warning withMessage(String message) {
        return new Warning(message);
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof Warning
                && Objects.equals(message, ((Warning) obj).message);
    }

    @Override
    public String toString() {
        return StringRepresentation.ofInstance(this)
                .add("message", message)
                .toString();
    }

}
