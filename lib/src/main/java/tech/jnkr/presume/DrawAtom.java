package tech.jnkr.presume;

sealed interface DrawAtom permits Trivial1, Trivial2, Regular, Edge {}

record Trivial1() implements DrawAtom {}

record Trivial2() implements DrawAtom {}

record Regular(float ratio, boolean sign, boolean simplify) implements DrawAtom {}

record Edge(float ratio, boolean sign) implements DrawAtom {}
