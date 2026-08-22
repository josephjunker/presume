package tech.jnkr.presume.harness;

import tech.jnkr.presume.AbstractGenerator;
import tech.jnkr.presume.generators.GenerationSource;

import java.util.List;

public class ExprGenerator extends AbstractGenerator<Expr> {
    @Override
    protected Expr gen(GenerationSource source) {
        boolean shouldTerminate = source.getWeightedBoolean(0.55f);

        if (shouldTerminate) return source.call(new LitGenerator());

        return source.oneOfLeftBiased(List.of(new AddGenerator(), new MulGenerator(), new NegGenerator()));
    }

    static class LitGenerator extends AbstractGenerator<Expr> {
        @Override
        protected Expr.Lit gen(GenerationSource source) {
            return new Expr.Lit(source.getInteger(-100, 100));
        }
    }

    static class AddGenerator extends AbstractGenerator<Expr> {
        @Override
        protected Expr.Add gen(GenerationSource source) {
            return new Expr.Add(source.call(new ExprGenerator()), source.call(new ExprGenerator()));
        }
    }

    static class MulGenerator extends AbstractGenerator<Expr> {
        @Override
        protected Expr.Mul gen(GenerationSource source) {
            return new Expr.Mul(source.call(new ExprGenerator()), source.call(new ExprGenerator()));
        }
    }

    static class NegGenerator extends AbstractGenerator<Expr> {
        @Override
        protected Expr.Neg gen(GenerationSource source) {
            return new Expr.Neg(source.call(new ExprGenerator()));
        }
    }
}
