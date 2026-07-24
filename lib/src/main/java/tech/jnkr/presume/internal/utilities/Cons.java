package tech.jnkr.presume.internal.utilities;

import java.util.Objects;

public record Cons<T>(T head, ImmutableList<T> tail) implements ImmutableList<T> {
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (!(other instanceof Cons<?>(Object otherHead, ImmutableList<?> otherTail))) return false;

        if (!head.equals(otherHead)) return false;

        return tail.equals(otherTail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(head.hashCode(), tail.hashCode());
    }
}
