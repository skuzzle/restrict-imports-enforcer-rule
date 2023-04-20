package de.skuzzle.enforcer.restrictimports.parser;

import java.util.Objects;

import de.skuzzle.enforcer.restrictimports.util.Preconditions;
import de.skuzzle.enforcer.restrictimports.util.StringRepresentation;

/**
 * Represents additional findings that shall be reported for an encountered file.
 *
 * @since 2.2.0
 */
public final class Annotation {
    private final String message;

    private Annotation(String message) {
        Preconditions.checkArgument(message != null, "message must not be null");
        this.message = message;
    }

    public static Annotation withMessage(String message) {
        return new Annotation(message);
    }

    public String getMessage() {
        return message;
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof Annotation &&
                Objects.equals(message, ((Annotation) obj).message);
    }

    @Override
    public String toString() {
        return StringRepresentation.ofInstance(this)
                .add("message", message)
                .toString();
    }
}
