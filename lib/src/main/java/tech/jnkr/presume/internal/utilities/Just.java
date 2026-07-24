package tech.jnkr.presume.internal.utilities;

public record Just<T>(T value) implements Maybe<T> {
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Just<?>(Object otherValue))) return false;
        return value.equals(otherValue);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
