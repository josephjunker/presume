package tech.jnkr.presume.integration;

import org.junit.jupiter.api.Test;

import tech.jnkr.presume.StatsCollector;

public class ExprGeneratorStatsTests {
    @Test
    public void example() {
        ExprGenerator generator = new ExprGenerator();
        StatsCollector<Expr> collector = new StatsCollector<>(generator);

        collector
                .addEnumBucket(
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
                .withMaximumRatio(Depth.ONE, 0.3f)
                .withMinimumRatio(Depth.TEN_PLUS, 0.05f)
                .withMinimumRatio(Depth.FIVE_TO_TEN, 0.05f);

        collector
                .addBooleanBucket("Contains Add", ExprOperations::containsAdd)
                .withMinimumTrueRatio(0.2f);
        collector
                .addBooleanBucket("Contains Mul", ExprOperations::containsMul)
                .withMinimumTrueRatio(0.2f);
        collector
                .addBooleanBucket("Contains Neg", ExprOperations::containsNeg)
                .withMinimumTrueRatio(0.2f);

        collector.summarize(10_000);
    }

    public enum Depth {
        ONE,
        TWO,
        TWO_TO_FIVE,
        FIVE_TO_TEN,
        TEN_PLUS
    }
}
