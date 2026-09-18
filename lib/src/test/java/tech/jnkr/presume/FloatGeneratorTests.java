package tech.jnkr.presume;

import org.junit.jupiter.api.Test;

import tech.jnkr.presume.internal.atoms.AtomSource;
import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.atoms.Edge;
import tech.jnkr.presume.internal.atoms.Regular;
import tech.jnkr.presume.internal.atoms.Trivial1;

import java.util.function.Predicate;

public class FloatGeneratorTests {
    private static final float DEFAULT_BOUND = Float.MAX_VALUE / 2f;

    @Test
    public void shouldProduceBothSignsWithSimilarFrequency() {
        FloatGenerator gen = unbounded(new FloatGenerator(FloatGeneratorTests::getRegular));

        StatsCollector<Float> collector = new StatsCollector<>(gen);
        collector
                .addBooleanBucket("signBitSet", FloatGeneratorTests::signBitSet)
                .withMinimumTrueRatio(0.3f)
                .withMinimumFalseRatio(0.3f);

        collector.summarize(10_000);
    }

    @Test
    public void shouldProduceBroadRangeOfMagnitudes() {
        FloatGenerator gen = unbounded(new FloatGenerator(FloatGeneratorTests::getRegular));

        StatsCollector<Float> collector = new StatsCollector<>(gen);

        // The raw exponent field is uniform over [0, 254], so every binade from
        // the subnormals up to 2^127 is roughly equally likely.
        var binades =
                collector.addExactIntegerBucket("binade", 0, 10, f -> exponentField(f) * 10 / 255);
        for (int i = 0; i < 10; i++) {
            binades.withMinimumRatio(i, 0.05f).withMaximumRatio(i, 0.2f);
        }

        collector
                .addBooleanBucket("tiny", f -> Math.abs(f) < 0x1p-100f)
                .withMinimumTrueRatio(0.04f);
        collector.addBooleanBucket("huge", f -> Math.abs(f) > 0x1p100f).withMinimumTrueRatio(0.04f);
        collector
                .addBooleanBucket("subnormal", f -> exponentField(f) == 0)
                .withMinimumTrueRatio(0.001f)
                .withMaximumTrueRatio(0.01f);

        collector.summarize(100_000);
    }

    @Test
    public void shouldProduceBroadRangeOfPrecisions() {
        FloatGenerator gen = unbounded(new FloatGenerator(FloatGeneratorTests::getRegular));

        StatsCollector<Float> collector = new StatsCollector<>(gen);
        collector
                .addBooleanBucket("mantissaLowBitSet", f -> (mantissa(f) & 1) != 0)
                .withMinimumTrueRatio(0.4f)
                .withMaximumTrueRatio(0.6f);
        collector
                .addBooleanBucket("fourOrMoreTrailingZeroBits", f -> (mantissa(f) & 0xF) == 0)
                .withMinimumTrueRatio(0.03f)
                .withMaximumTrueRatio(0.12f);
        collector
                .addBooleanBucket("eightOrMoreTrailingZeroBits", f -> (mantissa(f) & 0xFF) == 0)
                .withMinimumTrueRatio(0.001f)
                .withMaximumTrueRatio(0.01f);

        collector.summarize(100_000);
    }

    @Test
    public void shouldRespectAtomSign() {
        FloatGenerator positiveGen = unbounded(new FloatGenerator(() -> getRegularWithSign(true)));
        StatsCollector<Float> positiveCollector = new StatsCollector<>(positiveGen);
        positiveCollector
                .addBooleanBucket("signBitClear", f -> !signBitSet(f))
                .withMinimumTrueRatio(1.0f);
        positiveCollector.summarize(10_000);

        FloatGenerator negativeGen = unbounded(new FloatGenerator(() -> getRegularWithSign(false)));
        StatsCollector<Float> negativeCollector = new StatsCollector<>(negativeGen);
        negativeCollector
                .addBooleanBucket("signBitSet", FloatGeneratorTests::signBitSet)
                .withMinimumTrueRatio(1.0f);
        negativeCollector.summarize(10_000);
    }

    @Test
    public void shouldStayInRangeWhenRangeForcesSign() {
        FloatGenerator positiveRangeGen =
                new FloatGenerator(() -> getRegularWithSign(false))
                        .withMinimum(100f)
                        .withMaximum(9999999f);
        StatsCollector<Float> positiveCollector = new StatsCollector<>(positiveRangeGen);
        positiveCollector
                .addBooleanBucket("inRange", f -> f >= 100f && f <= 9999999f)
                .withMinimumTrueRatio(1.0f);
        positiveCollector.summarize(10_000);

        FloatGenerator negativeRangeGen =
                new FloatGenerator(() -> getRegularWithSign(true))
                        .withMinimum(-9999999f)
                        .withMaximum(-100f);
        StatsCollector<Float> negativeCollector = new StatsCollector<>(negativeRangeGen);
        negativeCollector
                .addBooleanBucket("inRange", f -> f >= -9999999f && f <= -100f)
                .withMinimumTrueRatio(1.0f);
        negativeCollector.summarize(10_000);
    }

    @Test
    public void shouldClampToMinimumAndMaximum() {
        AtomSource source = new AtomSource();
        FloatGenerator gen = new FloatGenerator(source::getAtom);
        StatsCollector<Float> collector = new StatsCollector<>(gen);
        collector
                .addBooleanBucket(
                        "inRangeOrNaN",
                        f -> Float.isNaN(f) || (f >= -DEFAULT_BOUND && f <= DEFAULT_BOUND))
                .withMinimumTrueRatio(1.0f);
        collector.summarize(10_000);

        AtomSource noNaNSource = new AtomSource();
        FloatGenerator noNaNGen = new FloatGenerator(noNaNSource::getAtom).disallowNaN();
        StatsCollector<Float> noNaNCollector = new StatsCollector<>(noNaNGen);
        noNaNCollector
                .addBooleanBucket("inRange", f -> f >= -DEFAULT_BOUND && f <= DEFAULT_BOUND)
                .withMinimumTrueRatio(1.0f);
        noNaNCollector.summarize(10_000);
    }

    @Test
    public void shouldProduceNaNOnlyWhenAllowed() {
        AtomSource source = new AtomSource();
        FloatGenerator gen = new FloatGenerator(source::getAtom);
        StatsCollector<Float> collector = new StatsCollector<>(gen);
        collector
                .addBooleanBucket("nan", f -> Float.isNaN(f))
                .withMinimumTrueRatio(0.003f)
                .withMaximumTrueRatio(0.03f);
        collector.summarize(10_000);

        AtomSource noNaNSource = new AtomSource();
        FloatGenerator noNaNGen = new FloatGenerator(noNaNSource::getAtom).disallowNaN();
        StatsCollector<Float> noNaNCollector = new StatsCollector<>(noNaNGen);
        noNaNCollector.addBooleanBucket("nan", f -> Float.isNaN(f)).withMaximumTrueRatio(0);
        noNaNCollector.summarize(10_000);
    }

    @Test
    public void shouldProduceInfinityOnlyWhenAllowed() {
        AtomSource source = new AtomSource();
        FloatGenerator gen = unbounded(new FloatGenerator(source::getAtom));
        StatsCollector<Float> collector = new StatsCollector<>(gen);
        collector
                .addBooleanBucket("infinite", f -> Float.isInfinite(f))
                .withMinimumTrueRatio(0.003f)
                .withMaximumTrueRatio(0.03f);
        collector.summarize(10_000);

        AtomSource noInfSource = new AtomSource();
        FloatGenerator noInfGen =
                unbounded(new FloatGenerator(noInfSource::getAtom)).disallowInfinity();
        StatsCollector<Float> noInfCollector = new StatsCollector<>(noInfGen);
        noInfCollector
                .addBooleanBucket("infinite", f -> Float.isInfinite(f))
                .withMaximumTrueRatio(0);
        noInfCollector.summarize(10_000);
    }

    @Test
    public void shouldProduceSpecialValuesFromEdgeAtoms() {
        assertAlways(new Edge(100, true), f -> !signBitSet(f) && f == 0f, "positiveZero");
        assertAlways(new Edge(100, false), f -> signBitSet(f) && f == 0f, "negativeZero");
        assertAlways(new Edge(600_000_000, true), f -> f == Float.MIN_VALUE, "minValue");
        assertAlways(
                new Edge(1_000_000_000, false), f -> f == -Float.MAX_VALUE, "negativeMax", true);
        assertAlways(
                new Edge(1_500_000_000, true), f -> f == Float.POSITIVE_INFINITY, "infinity", true);
        assertAlways(new Edge(Integer.MAX_VALUE, true), f -> Float.isNaN(f), "nan");
    }

    @Test
    public void shouldProduceApproachingFromTrivialAtoms() {
        FloatGenerator gen = new FloatGenerator(Trivial1::new).shrinkingTowards(12.5f);
        StatsCollector<Float> collector = new StatsCollector<>(gen);
        collector.addBooleanBucket("wasApproaching", f -> f == 12.5f).withMinimumTrueRatio(1.0f);

        collector.summarize(10_000);
    }

    @Test
    public void shouldProduceIntegralValuesFromSimplifiedAtoms() {
        FloatGenerator gen = new FloatGenerator(FloatGeneratorTests::getSimplifiedRegular);
        StatsCollector<Float> collector = new StatsCollector<>(gen);
        collector
                .addBooleanBucket("wasIntegral", f -> f == (float) Math.rint(f))
                .withMinimumTrueRatio(1.0f);

        collector.summarize(10_000);
    }

    @Test
    public void shouldConcentrateAtBoundsForNarrowRanges() {
        FloatGenerator gen =
                new FloatGenerator(FloatGeneratorTests::getRegular)
                        .withMinimum(1f)
                        .withMaximum(1000f);
        StatsCollector<Float> collector = new StatsCollector<>(gen);
        collector
                .addBooleanBucket("inRange", f -> f >= 1f && f <= 1000f)
                .withMinimumTrueRatio(1.0f);
        collector.addBooleanBucket("atMinimum", f -> f == 1f).withMinimumTrueRatio(0.3f);
        collector.addBooleanBucket("atMaximum", f -> f == 1000f).withMinimumTrueRatio(0.3f);
        collector
                .addBooleanBucket("strictlyBetween", f -> f > 1f && f < 1000f)
                .withMinimumTrueRatio(0.01f);

        collector.summarize(10_000);
    }

    private static void assertAlways(DrawAtom atom, Predicate<Float> predicate, String title) {
        assertAlways(atom, predicate, title, false);
    }

    private static void assertAlways(
            DrawAtom atom, Predicate<Float> predicate, String title, boolean unbounded) {
        FloatGenerator gen = new FloatGenerator(() -> atom);
        if (unbounded) gen = unbounded(gen);
        StatsCollector<Float> collector = new StatsCollector<>(gen);
        collector.addBooleanBucket(title, predicate::test).withMinimumTrueRatio(1.0f);
        collector.summarize(10_000);
    }

    private static FloatGenerator unbounded(FloatGenerator gen) {
        return gen.withMinimum(Float.NEGATIVE_INFINITY).withMaximum(Float.POSITIVE_INFINITY);
    }

    private static boolean signBitSet(float f) {
        return (Float.floatToRawIntBits(f) & 0x80000000) != 0;
    }

    private static int exponentField(float f) {
        return (Float.floatToRawIntBits(f) >>> 23) & 0xFF;
    }

    private static int mantissa(float f) {
        return Float.floatToRawIntBits(f) & 0x7FFFFF;
    }

    public static DrawAtom getRegular() {
        AtomSource source = new AtomSource();
        DrawAtom atom = source.getAtom();

        return atom instanceof Regular ? atom : getRegular();
    }

    private static DrawAtom getRegularWithSign(boolean sign) {
        AtomSource source = new AtomSource();
        DrawAtom atom = source.getAtom();

        if (atom instanceof Regular(int magnitude, _, boolean simplify)) {
            return new Regular(magnitude, sign, simplify);
        }
        return getRegularWithSign(sign);
    }

    private static DrawAtom getSimplifiedRegular() {
        AtomSource source = new AtomSource();
        DrawAtom atom = source.getAtom();

        if (atom instanceof Regular(int magnitude, boolean sign, _)) {
            return new Regular(magnitude, sign, true);
        }
        return getSimplifiedRegular();
    }
}
