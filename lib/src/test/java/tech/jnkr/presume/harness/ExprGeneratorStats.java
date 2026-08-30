package tech.jnkr.presume.harness;

import org.junit.jupiter.api.Test;

import tech.jnkr.presume.StatsCollector;

public class ExprGeneratorStats {
    @Test
    public void stats() {
        ExprGenerator generator = new ExprGenerator();
        StatsCollector<Expr> collector = new StatsCollector<>(generator);

        collector
                .withBucket(
                        Depth.class,
                        "Max depth",
                        expr -> {
                            int maxDepth = ExprOperations.maxDepth(expr);

                            if (maxDepth == 1) return Depth.ONE;
                            if (maxDepth == 2) return Depth.TWO;
                            if (maxDepth < 5) return Depth.TWO_TO_FIVE;
                            if (maxDepth < 10) return Depth.FIVE_TO_TEN;
                            return Depth.TEN_PLUS;
                        })
                .withBooleanBucket("Contains Add", ExprOperations::containsAdd)
                .withBooleanBucket("Contains Mul", ExprOperations::containsMul)
                .withBooleanBucket("Contains Neg", ExprOperations::containsNeg)
                .summarize(10_000);
    }

    public enum Depth {
        ONE,
        TWO,
        TWO_TO_FIVE,
        FIVE_TO_TEN,
        TEN_PLUS
    }
}
