package tech.jnkr.presume.internal.shrinking;

import tech.jnkr.presume.internal.atoms.DrawAtom;

public record Right(DrawAtom atom) implements TraceEntry {}
