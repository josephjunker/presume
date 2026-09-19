package tech.jnkr.presume;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import tech.jnkr.presume.internal.atoms.AtomSource;
import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.atoms.Edge;
import tech.jnkr.presume.internal.atoms.Regular;
import tech.jnkr.presume.internal.atoms.Trivial1;

import java.util.function.Function;

public class LongGeneratorTests {
    @ParameterizedTest
    @CsvSource({
        "10, 0, 0",
        "100, 0, 0",
        "4096, 0, 0",
        "9999999, 0, 0",
        "999999999999, 0, 0",
        "0, -100, 0",
        "100, 33, 0",
        "4096, 1024, 0",
        "9999999, 777, 0",
        "0, -100, 0",
        "100, -33, 0",
        "4096, -1024, 0",
        "9999999, -777, 0",
        "999999999999, -777777777777, 0",
        "10, 0, 5",
        "100, 0, 40",
        "4096, 0, 3333",
        "9999999, 0, 1",
        "0, -100, -25",
        "100, 33, 35",
        "4096, 1024, 2048",
        "9999999, 777, 33333",
        "0, -100, -98",
        "100, -33, -11",
        "4096, -1024, 1000",
        "9999999, -777, 10000",
    })
    public void shouldProduceBothOddAndEvenNumbers(long max, long min, long shrinkTowards) {
        LongGenerator gen = new LongGenerator(LongGeneratorTests::getRegular);
        gen = gen.withMaximumExclusive(max);
        gen = gen.withMinimumInclusive(min);
        if (shrinkTowards != 0) gen = gen.shrinkingTowards(shrinkTowards);

        StatsCollector<Long> collector = new StatsCollector<>(gen);
        collector
                .addBooleanBucket("wasEven", n -> n % 2 == 0)
                .withMinimumTrueRatio(0.3f)
                .withMinimumFalseRatio(0.3f);

        collector.summarize(10_000);
    }

    @ParameterizedTest
    @CsvSource({
        "10, 0, 0",
        "100, 0, 0",
        "4096, 0, 0",
        "9999999, 0, 0",
        "0, -100, 0",
        "100, 33, 0",
        "4096, 1024, 0",
        "9999999, 777, 0",
        "0, -100, 0",
        "100, -100, 0",
        "4096, -4096, 0",
        "777, -777, 0",
        "9223372036854775807, 0, 0",
        "0, -9223372036854775808, 0",
        "9223372036854775807, -9223372036854775808, 0",
        "10, 0, 5",
        "100, 0, 40",
        "4096, 0, 3333",
        "9999999, 0, 1",
        "0, -100, -25",
        "100, 33, 35",
        "4096, 1024, 2048",
        "9999999, 777, 33333",
        "0, -100, -98",
        "33, -33, -11",
        "1024, -1024, 1000",
        "9999999, -9999999, 10000",
    })
    public void shouldProduceRelativelyEvenDistribution(long max, long min, long shrinkTowards) {
        LongGenerator gen = new LongGenerator(LongGeneratorTests::getRegular);
        gen = gen.withMaximumExclusive(max);
        gen = gen.withMinimumInclusive(min);
        if (shrinkTowards != 0) gen = gen.shrinkingTowards(shrinkTowards);

        StatsCollector<Long> collector = new StatsCollector<>(gen);

        var buckets =
                collector.addExactIntegerBucket(
                        "Long distributions", 0, 10, bucketizer(min, max, 10));
        for (int i = 0; i < 10; i++) {
            buckets.withMinimumRatio(i, 0.05f).withMaximumRatio(i, 0.2f);
        }

        collector.summarize(100_000);
    }

    @Test
    public void shouldProduceEdgeValuesWithReasonableFrequency() {
        LongGenerator gen = new LongGenerator(LongGeneratorTests::getRegular);

        LongGenerator zeroToFive = gen.withMinimumInclusive(0).withMaximumExclusive(5);
        StatsCollector<Long> zeroToFiveCollector = new StatsCollector<>(zeroToFive);
        zeroToFiveCollector
                .addExactIntegerBucket("value", 0, 6, Long::intValue)
                .withMinimumRatio(4, 0.1f)
                .withMaximumRatio(5, 0);

        zeroToFiveCollector.summarize(10_000);
    }

    @Test
    public void shouldRespectAtomSignWhenRangeStraddlesZero() {
        LongGenerator positiveGen =
                new LongGenerator(() -> getRegularWithSign(true))
                        .withMinimumInclusive(-4096)
                        .withMaximumExclusive(4096);
        StatsCollector<Long> positiveCollector = new StatsCollector<>(positiveGen);
        positiveCollector.addBooleanBucket("nonNegative", n -> n >= 0).withMinimumTrueRatio(1.0f);
        positiveCollector.addBooleanBucket("positive", n -> n > 0).withMinimumTrueRatio(0.9f);
        positiveCollector.summarize(10_000);

        LongGenerator negativeGen =
                new LongGenerator(() -> getRegularWithSign(false))
                        .withMinimumInclusive(-4096)
                        .withMaximumExclusive(4096);
        StatsCollector<Long> negativeCollector = new StatsCollector<>(negativeGen);
        negativeCollector.addBooleanBucket("nonPositive", n -> n <= 0).withMinimumTrueRatio(1.0f);
        negativeCollector.addBooleanBucket("negative", n -> n < 0).withMinimumTrueRatio(0.9f);
        negativeCollector.summarize(10_000);
    }

    @Test
    public void shouldStayInRangeWhenRangeForcesSign() {
        LongGenerator positiveRangeGen =
                new LongGenerator(() -> getRegularWithSign(false))
                        .withMinimumInclusive(100)
                        .withMaximumExclusive(9999999);
        StatsCollector<Long> positiveCollector = new StatsCollector<>(positiveRangeGen);
        positiveCollector
                .addBooleanBucket("inRange", n -> n >= 100 && n < 9999999)
                .withMinimumTrueRatio(1.0f);
        positiveCollector.summarize(10_000);

        LongGenerator negativeRangeGen =
                new LongGenerator(() -> getRegularWithSign(true))
                        .withMinimumInclusive(-9999999)
                        .withMaximumExclusive(-100);
        StatsCollector<Long> negativeCollector = new StatsCollector<>(negativeRangeGen);
        negativeCollector
                .addBooleanBucket("inRange", n -> n >= -9999999 && n < -100)
                .withMinimumTrueRatio(1.0f);
        negativeCollector.summarize(10_000);
    }

    @Test
    public void shouldProduceWideRangeOfValuesAcrossFullLongRange() {
        LongGenerator gen = new LongGenerator(LongGeneratorTests::getRegular);
        StatsCollector<Long> collector = new StatsCollector<>(gen);
        collector.addBooleanBucket("huge", n -> n > (1L << 40)).withMinimumTrueRatio(0.3f);
        collector.addBooleanBucket("hugeNegative", n -> n < -(1L << 40)).withMinimumTrueRatio(0.3f);

        collector.summarize(100_000);
    }

    @Test
    public void shouldProduceRangeExtremesFromEdgeAtoms() {
        LongGenerator minGen =
                new LongGenerator(() -> new Edge(Integer.MAX_VALUE, true))
                        .withMinimumInclusive(-100)
                        .withMaximumExclusive(100);
        StatsCollector<Long> minCollector = new StatsCollector<>(minGen);
        minCollector.addBooleanBucket("wasMinimum", n -> n == -100).withMinimumTrueRatio(1.0f);
        minCollector.summarize(10_000);

        LongGenerator maxGen =
                new LongGenerator(() -> new Edge(Integer.MAX_VALUE, false))
                        .withMinimumInclusive(-100)
                        .withMaximumExclusive(100);
        StatsCollector<Long> maxCollector = new StatsCollector<>(maxGen);
        maxCollector
                .addBooleanBucket("wasMaximumExclusive", n -> n == 99)
                .withMinimumTrueRatio(1.0f);
        maxCollector.summarize(10_000);
    }

    @Test
    public void shouldProduceApproachingFromTrivialAtoms() {
        LongGenerator gen = new LongGenerator(Trivial1::new).shrinkingTowards(1234);
        StatsCollector<Long> collector = new StatsCollector<>(gen);
        collector.addBooleanBucket("wasApproaching", n -> n == 1234).withMinimumTrueRatio(1.0f);

        collector.summarize(10_000);
    }

    @Test
    public void shouldProduceSingleValueWhenRangeHasOneValue() {
        LongGenerator gen =
                new LongGenerator(LongGeneratorTests::getRegular)
                        .withMinimumInclusive(41)
                        .withMaximumExclusive(42);
        StatsCollector<Long> collector = new StatsCollector<>(gen);
        collector.addBooleanBucket("was41", n -> n == 41).withMinimumTrueRatio(1.0f);

        collector.summarize(10_000);
    }

    public static DrawAtom getRegular() {
        AtomSource source = new AtomSource();
        DrawAtom atom = source.getAtom();

        return atom instanceof Regular ? atom : getRegular();
    }

    private static DrawAtom getRegularWithSign(boolean sign) {
        AtomSource source = new AtomSource();
        DrawAtom atom = source.getAtom();

        if (atom instanceof Regular(int magnitude, boolean ignoredSign, boolean simplify)) {
            return new Regular(magnitude, sign, simplify);
        }
        return getRegularWithSign(sign);
    }

    private static Function<Long, Integer> bucketizer(long min, long max, int buckets) {
        double width = ((double) max - (double) min) / buckets;
        return value -> Math.min(buckets - 1, (int) (((double) value - (double) min) / width));
    }
}
