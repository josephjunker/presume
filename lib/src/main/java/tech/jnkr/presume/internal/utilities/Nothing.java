package tech.jnkr.presume.internal.utilities;

public record Nothing<T>() implements Maybe<T> {
    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        return other instanceof Nothing<?>;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
