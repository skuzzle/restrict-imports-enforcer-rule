package de.skuzzle.enforcer.restrictimports.cd;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public final class CycleParticipant {

    private final String fqcn;

    public CycleParticipant(String fqcn) {
        this.fqcn = fqcn;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("fqcn", fqcn)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(fqcn);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof CycleParticipant
                && Objects.equals(fqcn, ((CycleParticipant) obj).fqcn);
    }
}
