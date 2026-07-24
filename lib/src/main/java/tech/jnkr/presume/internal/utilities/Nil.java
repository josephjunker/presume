package tech.jnkr.presume.internal.utilities;

public record Nil<T>() implements ImmutableList<T> {
    @Override
    public boolean equals(Object other) {
        if (other == this) return true;

        return other instanceof Nil<?>;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
