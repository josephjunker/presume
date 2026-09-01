package tech.jnkr.presume.internal.atoms;

import java.io.Serializable;

public sealed interface DrawAtom extends Serializable permits Trivial1, Trivial2, Regular, Edge {}
