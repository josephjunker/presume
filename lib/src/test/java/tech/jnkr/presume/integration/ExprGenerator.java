package tech.jnkr.presume.integration;

import org.jspecify.annotations.NonNull;
import tech.jnkr.presume.AbstractGenerator;
import tech.jnkr.presume.GenerationSource;

import java.util.List;

public class ExprGenerator extends AbstractGenerator<Expr> {
    @Override
    protected Expr gen(@NonNull GenerationSource source) {
        return source.oneOfLeftBiased(
                List.of(
                        new LitGenerator(),
                        new AddGenerator(),
                        new MulGenerator(),
                        new NegGenerator()));
    }

    public static AbstractGenerator<Expr> recursiveGenerator =
            new AbstractGenerator<Expr>() {
                @Override
                protected Expr gen(@NonNull GenerationSource source) {
                    boolean shouldRecurse = source.getWeightedBoolean(0.45f);

                    if (!shouldRecurse) return source.call(new LitGenerator());

                    return source.oneOfLeftBiased(
                            List.of(
                                    new LitGenerator(),
                                    new AddGenerator(),
                                    new MulGenerator(),
                                    new NegGenerator()));
                }
            };

    static class LitGenerator extends AbstractGenerator<Expr> {
        @Override
        protected Expr.Lit gen(@NonNull GenerationSource source) {
            return new Expr.Lit(source.getInteger(-100, 100));
        }
    }

    static class AddGenerator extends AbstractGenerator<Expr> {
        @Override
        protected Expr.Add gen(@NonNull GenerationSource source) {
            return new Expr.Add(source.call(recursiveGenerator), source.call(recursiveGenerator));
        }
    }

    static class MulGenerator extends AbstractGenerator<Expr> {
        @Override
        protected Expr.Mul gen(@NonNull GenerationSource source) {
            return new Expr.Mul(source.call(recursiveGenerator), source.call(recursiveGenerator));
        }
    }

    static class NegGenerator extends AbstractGenerator<Expr> {
        @Override
        protected Expr.Neg gen(@NonNull GenerationSource source) {

            return new Expr.Neg(source.call(recursiveGenerator));
        }
    }
}
