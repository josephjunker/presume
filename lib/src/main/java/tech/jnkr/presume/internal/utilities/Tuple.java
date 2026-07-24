package tech.jnkr.presume.internal.utilities;

import java.util.Objects;

public record Tuple<A, B>(A first, B second) {
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Tuple<?, ?>(var otherFirst, var otherSecond))) return false;
        return first.equals(otherFirst) && second.equals(otherSecond);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first.hashCode(), second.hashCode());
    }
}
