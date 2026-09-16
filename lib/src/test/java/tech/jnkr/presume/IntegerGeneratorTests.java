package tech.jnkr.presume;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import tech.jnkr.presume.internal.atoms.AtomSource;
import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.atoms.Regular;

public class IntegerGeneratorTests {
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
        "100, -33, 0",
        "4096, -1024, 0",
        "9999999, -777, 0",
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
    public void shouldProduceBothOddAndEvenNumbers(int max, int min, int shrinkTowards) {
        IntegerGenerator gen = new IntegerGenerator(IntegerGeneratorTests::getRegular);
        gen = gen.withMaximum(max);
        gen = gen.withMinimum(min);
        if (shrinkTowards != 0) gen = gen.shrinkingTowards(shrinkTowards);

        StatsCollector<Integer> collector = new StatsCollector<>(gen);
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
    public void shouldProduceRelativelyEvenDistribution(int max, int min, int shrinkTowards) {
        IntegerGenerator gen = new IntegerGenerator(IntegerGeneratorTests::getRegular);
        gen = gen.withMaximum(max);
        gen = gen.withMinimum(min);
        if (shrinkTowards != 0) gen = gen.shrinkingTowards(shrinkTowards);

        StatsCollector<Integer> collector = new StatsCollector<>(gen);

        var buckets = collector.addIntegerBucket("Integer distributions", min, max, 10, x -> x);
        for (int i = 0; i < 10; i++) {
            buckets.withMinimumRatio(i, 0.05f).withMaximumRatio(i, 0.2f);
        }

        collector.summarize(100_000);
    }

    public static DrawAtom getRegular() {
        AtomSource source = new AtomSource();
        DrawAtom atom = source.getAtom();

        return atom instanceof Regular ? atom : getRegular();
    }
}
