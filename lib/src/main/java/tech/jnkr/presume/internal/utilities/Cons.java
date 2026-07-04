package tech.jnkr.presume.internal.utilities;

public record Cons<T>(T head, ImmutableList<T> tail) implements ImmutableList<T> {}
