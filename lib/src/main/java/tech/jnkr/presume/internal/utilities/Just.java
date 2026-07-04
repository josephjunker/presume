package tech.jnkr.presume.internal.utilities;

public record Just<T>(T value) implements Maybe<T> {}
