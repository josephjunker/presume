package tech.jnkr.presume.integration;

public sealed interface Expr {
    record Lit(int x) implements Expr {}

    record Add(Expr left, Expr right) implements Expr {}

    record Mul(Expr left, Expr right) implements Expr {}

    record Neg(Expr inner) implements Expr {}
}
