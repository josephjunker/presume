package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.InvalidGeneratorException;
import tech.jnkr.presume.internal.atoms.*;

import java.util.function.Supplier;

public class FloatGenerator extends AbstractGenerator<Float> {
    private final float minimum;
    private final float maximum;
    private final float approaching;
    private final boolean allowNaN;
    private final boolean allowInfinity;
    private final Supplier<DrawAtom> atomSupplier;

    private static final int oneFifthMaxInt = Integer.MAX_VALUE / 5;
    private static final int twoFifthsMaxInt = oneFifthMaxInt * 2;
    private static final int threeFifthsMaxInt = oneFifthMaxInt * 3;
    private static final int fourFifthsMaxInt = oneFifthMaxInt * 4;

    private static final int mantissaRange = 2 ^ 23;
    private static final float mantissaMaxIntRatio = (float) mantissaRange / Integer.MAX_VALUE;
    private static final int exponentRange = 2 ^ 8;
    private static final float exponentMaxIntRatio = (float) exponentRange / Integer.MAX_VALUE;

    FloatGenerator(Supplier<DrawAtom> atomSupplier) {
        this.atomSupplier = atomSupplier;
        minimum = -Float.MAX_VALUE / 2f;
        maximum = Float.MAX_VALUE / 2f;
        approaching = 0f;
        allowNaN = true;
        allowInfinity = true;
    }

    private FloatGenerator(
            Supplier<DrawAtom> atomSupplier,
            float minimum,
            float maximum,
            float approaching,
            boolean allowNaN,
            boolean allowInfinity) {
        this.atomSupplier = atomSupplier;
        this.minimum = minimum;
        this.maximum = maximum;
        this.approaching = approaching;
        this.allowNaN = allowNaN;
        this.allowInfinity = allowInfinity;
    }

    public FloatGenerator withMinimum(float minimum) {
        return new FloatGenerator(
                atomSupplier, minimum, maximum, approaching, allowNaN, allowInfinity);
    }

    public FloatGenerator withMaximum(float maximum) {
        return new FloatGenerator(
                atomSupplier, minimum, maximum, approaching, allowNaN, allowInfinity);
    }

    public FloatGenerator shrinkingTowards(float approaching) {
        return new FloatGenerator(
                atomSupplier, minimum, maximum, approaching, allowNaN, allowInfinity);
    }

    public FloatGenerator allowNaN() {
        return new FloatGenerator(atomSupplier, minimum, maximum, approaching, true, allowInfinity);
    }

    public FloatGenerator disallowNaN() {
        return new FloatGenerator(
                atomSupplier, minimum, maximum, approaching, false, allowInfinity);
    }

    public FloatGenerator allowInfinity() {
        return new FloatGenerator(atomSupplier, minimum, maximum, approaching, allowNaN, true);
    }

    public FloatGenerator disallowInfinity() {
        return new FloatGenerator(atomSupplier, minimum, maximum, approaching, allowNaN, false);
    }

    public Float gen() {
        return produce(atomSupplier.get(), atomSupplier.get());
    }

    private Float produce(DrawAtom atom1, DrawAtom atom2) {
        if (minimum > maximum)
            throw new InvalidGeneratorException(
                    String.format(
                            "Encountered a Float generator with a minimum greater than its"
                                    + " maximum. Minimum value: %f, maximum value: %f",
                            minimum, maximum));

        float result =
                switch (atom1) {
                    case Trivial1() -> 0f;
                    case Trivial2() -> 1f;
                    case Regular(int magnitude, boolean sign, boolean simplify) ->
                            generateFromSecondAtom(magnitude, atom2, sign, simplify);
                    case Edge(int magnitude, boolean sign) -> {
                        if (magnitude < oneFifthMaxInt) yield sign ? 0f : -0f;
                        if (magnitude < twoFifthsMaxInt)
                            yield sign ? Float.MIN_VALUE : -Float.MIN_VALUE;
                        if (magnitude < threeFifthsMaxInt)
                            yield sign ? Float.MAX_VALUE : -Float.MAX_VALUE;
                        if (magnitude < fourFifthsMaxInt) {
                            if (!allowInfinity)
                                yield generateFromSecondAtom(magnitude, atom2, sign, false);
                            yield sign ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
                        }
                        if (allowNaN) yield Float.NaN;
                        if (allowInfinity)
                            yield sign ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
                        yield generateFromSecondAtom(magnitude, atom2, sign, false);
                    }
                };
        if (result < minimum) return minimum;
        return Math.min(result, maximum);
    }

    private int magnitudeToMantissa(int magnitude) {
        return (int) ((float) magnitude * mantissaMaxIntRatio);
    }

    private int magnitudeToExponent(int magnitude) {
        return ((int) ((float) magnitude * exponentMaxIntRatio)) + 127;
    }

    private int signToInt(boolean sign) {
        return sign ? 1 : 0;
    }

    private float generateFromSecondAtom(
            int firstMagnitude, DrawAtom secondAtom, boolean sign, boolean simplify) {
        return switch (secondAtom) {
            case Trivial1() -> 0f;
            case Trivial2() -> 1f;
            case Regular(int secondMagnitude, boolean secondSign, boolean secondSimplify) ->
                    generateFromMagnitudes(firstMagnitude, secondMagnitude, sign, simplify);
            case Edge(int secondMagnitude, boolean sign1) -> {
                if (secondMagnitude < oneFifthMaxInt) yield sign ? 0f : -0f;
                if (secondMagnitude < twoFifthsMaxInt)
                    yield sign ? Float.MIN_VALUE : -Float.MIN_VALUE;
                if (secondMagnitude < threeFifthsMaxInt)
                    yield sign ? approaching + 1 : approaching - 1;
                if (secondMagnitude < fourFifthsMaxInt) {
                    if (!allowInfinity)
                        yield this.generateFromMagnitudes(
                                firstMagnitude, secondMagnitude, sign, false);
                    yield sign ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
                }
                if (allowNaN) yield Float.NaN;
                if (allowInfinity) yield Float.POSITIVE_INFINITY;
                yield generateFromMagnitudes(firstMagnitude, secondMagnitude, sign, false);
            }
        };
    }

    private float generateFromMagnitudes(
            int magnitude1, int magnitude2, boolean sign, boolean simplify) {
        boolean effectiveSign = (minimum >= 0) || sign;
        if (maximum <= 0) effectiveSign = false;

        int signInt = signToInt(effectiveSign);
        int exponentInt = magnitudeToExponent(magnitude1);
        int mantissaInt = magnitudeToMantissa(magnitude2);

        int bits = signInt << 31;
        bits |= exponentInt << 23;
        bits |= mantissaInt;

        float result = Float.intBitsToFloat(bits);
        if (simplify) return (float) (int) result;
        return result;
    }

    @Override
    protected Float gen(GenerationSource source) {
        return produce(atomSupplier.get(), atomSupplier.get());
    }
}
