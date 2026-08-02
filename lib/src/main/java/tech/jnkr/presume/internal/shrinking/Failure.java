package tech.jnkr.presume.internal.shrinking;

import tech.jnkr.presume.internal.Trace;

public record Failure<T>(T counterexample, Trace trace, Throwable exception) {}
