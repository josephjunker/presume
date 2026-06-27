package tech.jnkr.presume;

sealed interface DrawAtom permits Trivial1, Trivial2, Regular, Edge1, Edge2, Edge3, Edge4, Edge5 {}

record Trivial1() implements DrawAtom {}

record Trivial2() implements DrawAtom {}

record Regular(float ratio, boolean isPositive, boolean reduce) implements DrawAtom {}

record Edge1(float ratio) implements DrawAtom {}

record Edge2(float ratio) implements DrawAtom {}

record Edge3(float ratio) implements DrawAtom {}

record Edge4(float ratio) implements DrawAtom {}

record Edge5(float ratio) implements DrawAtom {}
