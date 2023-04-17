package de.skuzzle.enforcer.restrictimports.analyze;

import de.skuzzle.enforcer.restrictimports.util.StringRepresentation;

import java.util.Objects;

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
