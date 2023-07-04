package de.skuzzle.enforcer.restrictimports.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class StringRepresentation {

    private final String name;
    private final List<Attribute> attributes = new ArrayList<>();

    private StringRepresentation(String name) {
        this.name = name;
    }

    public static StringRepresentation ofType(Class<?> type) {
        return new StringRepresentation(type.getSimpleName());
    }

    public static StringRepresentation ofInstance(Object instance) {
        return ofType(instance.getClass());
    }

    public StringRepresentation add(String attribute, Object value) {
        attributes.add(new Attribute(attribute, value));
        return this;
    }

    @Override
    public String toString() {
        return new StringBuilder(name)
                .append("{")
                .append(attributes.stream().map(Attribute::toString).collect(Collectors.joining(", ")))
                .append("}")
                .toString();
    }

    private static final class Attribute {
        private final String name;
        private final Object value;

        private Attribute(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return name + "=" + value;
        }
    }
}
