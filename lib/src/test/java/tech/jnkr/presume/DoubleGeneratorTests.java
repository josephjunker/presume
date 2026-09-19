package tech.jnkr.presume;

import org.junit.jupiter.api.Test;

import tech.jnkr.presume.internal.atoms.AtomSource;
import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.atoms.Edge;
import tech.jnkr.presume.internal.atoms.Regular;
import tech.jnkr.presume.internal.atoms.Trivial1;

import java.util.function.Predicate;

public class DoubleGeneratorTests {
    private static final double DEFAULT_BOUND = Double.MAX_VALUE;

    @Test
    public void shouldProduceBothSignsWithSimilarFrequency() {
        DoubleGenerator gen = unbounded(new DoubleGenerator(DoubleGeneratorTests::getRegular));

        StatsCollector<Double> collector = new StatsCollector<>(gen);
        collector
                .addBooleanBucket("signBitSet", DoubleGeneratorTests::signBitSet)
                .withMinimumTrueRatio(0.3f)
                .withMinimumFalseRatio(0.3f);

        collector.summarize(10_000);
    }

    @Test
    public void shouldProduceBroadRangeOfMagnitudes() {
        DoubleGenerator gen = unbounded(new DoubleGenerator(DoubleGeneratorTests::getRegular));

        StatsCollector<Double> collector = new StatsCollector<>(gen);

        // The raw exponent field is uniform over [0, 2046], so every binade from
        // the subnormals up to 2^1023 is roughly equally likely.
        var binades =
                collector.addExactIntegerBucket("binade", 0, 10, d -> exponentField(d) * 10 / 2047);
        for (int i = 0; i < 10; i++) {
            binades.withMinimumRatio(i, 0.05f).withMaximumRatio(i, 0.2f);
        }

        collector.addBooleanBucket("tiny", d -> Math.abs(d) < 0x1p-500).withMinimumTrueRatio(0.15f);
        collector.addBooleanBucket("huge", d -> Math.abs(d) > 0x1p500).withMinimumTrueRatio(0.15f);
        collector
                .addBooleanBucket("subnormal", d -> exponentField(d) == 0)
                .withMinimumTrueRatio(0.0001f)
                .withMaximumTrueRatio(0.002f);

        collector.summarize(100_000);
    }

    @Test
    public void shouldProduceBroadRangeOfPrecisions() {
        DoubleGenerator gen = unbounded(new DoubleGenerator(DoubleGeneratorTests::getRegular));

        StatsCollector<Double> collector = new StatsCollector<>(gen);
        collector
                .addBooleanBucket("mantissaLowBitSet", d -> (mantissa(d) & 1) != 0)
                .withMinimumTrueRatio(0.4f)
                .withMaximumTrueRatio(0.6f);
        collector
                .addBooleanBucket("fourOrMoreTrailingZeroBits", d -> (mantissa(d) & 0xF) == 0)
                .withMinimumTrueRatio(0.03f)
                .withMaximumTrueRatio(0.12f);
        collector
                .addBooleanBucket("eightOrMoreTrailingZeroBits", d -> (mantissa(d) & 0xFF) == 0)
                .withMinimumTrueRatio(0.001f)
                .withMaximumTrueRatio(0.01f);

        collector.summarize(100_000);
    }

    @Test
    public void shouldRespectAtomSign() {
        DoubleGenerator positiveGen =
                unbounded(new DoubleGenerator(() -> getRegularWithSign(true)));
        StatsCollector<Double> positiveCollector = new StatsCollector<>(positiveGen);
        positiveCollector
                .addBooleanBucket("signBitClear", d -> !signBitSet(d))
                .withMinimumTrueRatio(1.0f);
        positiveCollector.summarize(10_000);

        DoubleGenerator negativeGen =
                unbounded(new DoubleGenerator(() -> getRegularWithSign(false)));
        StatsCollector<Double> negativeCollector = new StatsCollector<>(negativeGen);
        negativeCollector
                .addBooleanBucket("signBitSet", DoubleGeneratorTests::signBitSet)
                .withMinimumTrueRatio(1.0f);
        negativeCollector.summarize(10_000);
    }

    @Test
    public void shouldStayInRangeWhenRangeForcesSign() {
        DoubleGenerator positiveRangeGen =
                new DoubleGenerator(() -> getRegularWithSign(false))
                        .withMinimum(100)
                        .withMaximum(9999999);
        StatsCollector<Double> positiveCollector = new StatsCollector<>(positiveRangeGen);
        positiveCollector
                .addBooleanBucket("inRange", d -> d >= 100 && d <= 9999999)
                .withMinimumTrueRatio(1.0f);
        positiveCollector.summarize(10_000);

        DoubleGenerator negativeRangeGen =
                new DoubleGenerator(() -> getRegularWithSign(true))
                        .withMinimum(-9999999)
                        .withMaximum(-100);
        StatsCollector<Double> negativeCollector = new StatsCollector<>(negativeRangeGen);
        negativeCollector
                .addBooleanBucket("inRange", d -> d >= -9999999 && d <= -100)
                .withMinimumTrueRatio(1.0f);
        negativeCollector.summarize(10_000);
    }

    @Test
    public void shouldClampToMinimumAndMaximum() {
        AtomSource source = new AtomSource();
        DoubleGenerator gen = new DoubleGenerator(source::getAtom);
        StatsCollector<Double> collector = new StatsCollector<>(gen);
        collector
                .addBooleanBucket(
                        "inRangeOrNaN",
                        d -> Double.isNaN(d) || (d >= -DEFAULT_BOUND && d <= DEFAULT_BOUND))
                .withMinimumTrueRatio(1.0f);
        collector.summarize(10_000);

        AtomSource noNaNSource = new AtomSource();
        DoubleGenerator noNaNGen = new DoubleGenerator(noNaNSource::getAtom).disallowNaN();
        StatsCollector<Double> noNaNCollector = new StatsCollector<>(noNaNGen);
        noNaNCollector
                .addBooleanBucket("inRange", d -> d >= -DEFAULT_BOUND && d <= DEFAULT_BOUND)
                .withMinimumTrueRatio(1.0f);
        noNaNCollector.summarize(10_000);
    }

    @Test
    public void shouldProduceNaNOnlyWhenAllowed() {
        AtomSource source = new AtomSource();
        DoubleGenerator gen = new DoubleGenerator(source::getAtom);
        StatsCollector<Double> collector = new StatsCollector<>(gen);
        collector
                .addBooleanBucket("nan", d -> Double.isNaN(d))
                .withMinimumTrueRatio(0.003f)
                .withMaximumTrueRatio(0.03f);
        collector.summarize(10_000);

        AtomSource noNaNSource = new AtomSource();
        DoubleGenerator noNaNGen = new DoubleGenerator(noNaNSource::getAtom).disallowNaN();
        StatsCollector<Double> noNaNCollector = new StatsCollector<>(noNaNGen);
        noNaNCollector.addBooleanBucket("nan", d -> Double.isNaN(d)).withMaximumTrueRatio(0);
        noNaNCollector.summarize(10_000);
    }

    @Test
    public void shouldProduceInfinityOnlyWhenAllowed() {
        AtomSource source = new AtomSource();
        DoubleGenerator gen = unbounded(new DoubleGenerator(source::getAtom));
        StatsCollector<Double> collector = new StatsCollector<>(gen);
        collector
                .addBooleanBucket("infinite", d -> Double.isInfinite(d))
                .withMinimumTrueRatio(0.003f)
                .withMaximumTrueRatio(0.03f);
        collector.summarize(10_000);

        AtomSource noInfSource = new AtomSource();
        DoubleGenerator noInfGen =
                unbounded(new DoubleGenerator(noInfSource::getAtom)).disallowInfinity();
        StatsCollector<Double> noInfCollector = new StatsCollector<>(noInfGen);
        noInfCollector
                .addBooleanBucket("infinite", d -> Double.isInfinite(d))
                .withMaximumTrueRatio(0);
        noInfCollector.summarize(10_000);
    }

    @Test
    public void shouldProduceSpecialValuesFromEdgeAtoms() {
        assertAlways(new Edge(100, true), d -> !signBitSet(d) && d == 0, "positiveZero");
        assertAlways(new Edge(100, false), d -> signBitSet(d) && d == 0, "negativeZero");
        assertAlways(new Edge(600_000_000, true), d -> d == Double.MIN_VALUE, "minValue");
        assertAlways(new Edge(1_000_000_000, false), d -> d == -Double.MAX_VALUE, "negativeMax");
        assertAlways(
                new Edge(1_500_000_000, true),
                d -> d == Double.POSITIVE_INFINITY,
                "infinity",
                true);
        assertAlways(new Edge(Integer.MAX_VALUE, true), d -> Double.isNaN(d), "nan");
    }

    @Test
    public void shouldProduceApproachingFromTrivialAtoms() {
        DoubleGenerator gen = new DoubleGenerator(Trivial1::new).shrinkingTowards(12.5);
        StatsCollector<Double> collector = new StatsCollector<>(gen);
        collector.addBooleanBucket("wasApproaching", d -> d == 12.5).withMinimumTrueRatio(1.0f);

        collector.summarize(10_000);
    }

    @Test
    public void shouldProduceIntegralValuesFromSimplifiedAtoms() {
        DoubleGenerator gen = new DoubleGenerator(DoubleGeneratorTests::getSimplifiedRegular);
        StatsCollector<Double> collector = new StatsCollector<>(gen);
        collector
                .addBooleanBucket("wasIntegral", d -> d == Math.rint(d))
                .withMinimumTrueRatio(1.0f);

        collector.summarize(10_000);
    }

    @Test
    public void shouldConcentrateAtBoundsForNarrowRanges() {
        DoubleGenerator gen =
                new DoubleGenerator(DoubleGeneratorTests::getRegular)
                        .withMinimum(1)
                        .withMaximum(1000);
        StatsCollector<Double> collector = new StatsCollector<>(gen);
        collector.addBooleanBucket("inRange", d -> d >= 1 && d <= 1000).withMinimumTrueRatio(1.0f);
        collector.addBooleanBucket("atMinimum", d -> d == 1).withMinimumTrueRatio(0.3f);
        collector.addBooleanBucket("atMaximum", d -> d == 1000).withMinimumTrueRatio(0.3f);
        collector
                .addBooleanBucket("strictlyBetween", d -> d > 1 && d < 1000)
                .withMinimumTrueRatio(0.001f);

        collector.summarize(10_000);
    }

    private static void assertAlways(DrawAtom atom, Predicate<Double> predicate, String title) {
        assertAlways(atom, predicate, title, false);
    }

    private static void assertAlways(
            DrawAtom atom, Predicate<Double> predicate, String title, boolean unbounded) {
        DoubleGenerator gen = new DoubleGenerator(() -> atom);
        if (unbounded) gen = unbounded(gen);
        StatsCollector<Double> collector = new StatsCollector<>(gen);
        collector.addBooleanBucket(title, predicate::test).withMinimumTrueRatio(1.0f);
        collector.summarize(10_000);
    }

    private static DoubleGenerator unbounded(DoubleGenerator gen) {
        return gen.withMinimum(Double.NEGATIVE_INFINITY).withMaximum(Double.POSITIVE_INFINITY);
    }

    private static boolean signBitSet(double d) {
        return (Double.doubleToRawLongBits(d) & 0x8000000000000000L) != 0;
    }

    private static int exponentField(double d) {
        return (int) ((Double.doubleToRawLongBits(d) >>> 52) & 0x7FF);
    }

    private static long mantissa(double d) {
        return Double.doubleToRawLongBits(d) & 0xFFFFFFFFFFFFFL;
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

    private static DrawAtom getSimplifiedRegular() {
        AtomSource source = new AtomSource();
        DrawAtom atom = source.getAtom();

        if (atom instanceof Regular(int magnitude, boolean sign, boolean ignoredSimplify)) {
            return new Regular(magnitude, sign, true);
        }
        return getSimplifiedRegular();
    }
}
