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
        "0, 0, 0",
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
        "0, 0, 99999",
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
        if (max != 0) gen = gen.withMaximum(max);
        if (min != 0) gen = gen.withMinimum(min);
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
        "0, 0, 0",
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
        "0, 0, 99999",
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
    public void shouldProduceRelativelyEvenDistribution(int min, int max, int shrinkTowards) {
        IntegerGenerator gen = new IntegerGenerator(IntegerGeneratorTests::getRegular);
        if (max != 0) gen = gen.withMaximum(max);
        if (min != 0) gen = gen.withMinimum(min);
        if (shrinkTowards != 0) gen = gen.shrinkingTowards(shrinkTowards);
    }

    public static DrawAtom getRegular() {
        AtomSource source = new AtomSource();
        DrawAtom atom = source.getAtom();

        return atom instanceof Regular ? atom : getRegular();
    }
}
