package tech.jnkr.presume.internal.generators;

import tech.jnkr.presume.exceptions.InvalidGeneratorException;
import tech.jnkr.presume.generators.DoubleGenerator;
import tech.jnkr.presume.internal.atoms.*;

import java.util.function.Supplier;

public class ConcreteDoubleGenerator implements SimpleGenerator<Double>, DoubleGenerator {
    private final double minimum;
    private final double maximum;
    private final double approaching;
    private final boolean allowNaN;
    private final boolean allowInfinity;
    private final Supplier<DrawAtom> atomSupplier;

    private static final int oneFifthMaxInt = Integer.MAX_VALUE / 5;
    private static final int twoFifthsMaxInt = oneFifthMaxInt * 2;
    private static final int threeFifthsMaxInt = oneFifthMaxInt * 3;
    private static final int fourFifthsMaxInt = oneFifthMaxInt * 4;

    private static final long mantissaRange = 2 ^ 52;
    private static final double mantissaMaxLongRatio = (double) mantissaRange / Long.MAX_VALUE;
    private static final int exponentRange = 2 ^ 8;
    private static final double exponentMaxIntRatio = (double) exponentRange / Integer.MAX_VALUE;

    public ConcreteDoubleGenerator(Supplier<DrawAtom> atomSupplier) {
        this.atomSupplier = atomSupplier;
        minimum = -Double.MAX_VALUE;
        maximum = Double.MAX_VALUE;
        approaching = 0f;
        allowNaN = true;
        allowInfinity = true;
    }

    private ConcreteDoubleGenerator(
            Supplier<DrawAtom> atomSupplier,
            double minimum,
            double maximum,
            double approaching,
            boolean allowNaN,
            boolean allowInfinity) {
        this.atomSupplier = atomSupplier;
        this.minimum = minimum;
        this.maximum = maximum;
        this.approaching = approaching;
        this.allowNaN = allowNaN;
        this.allowInfinity = allowInfinity;
    }

    public ConcreteDoubleGenerator withMinimum(double minimum) {
        return new ConcreteDoubleGenerator(
                atomSupplier, minimum, maximum, approaching, allowNaN, allowInfinity);
    }

    public ConcreteDoubleGenerator withMaximum(double maximum) {
        return new ConcreteDoubleGenerator(
                atomSupplier, minimum, maximum, approaching, allowNaN, allowInfinity);
    }

    public ConcreteDoubleGenerator shrinkingTowards(double approaching) {
        return new ConcreteDoubleGenerator(
                atomSupplier, minimum, maximum, approaching, allowNaN, allowInfinity);
    }

    public ConcreteDoubleGenerator allowNaN() {
        return new ConcreteDoubleGenerator(
                atomSupplier, minimum, maximum, approaching, true, allowInfinity);
    }

    public ConcreteDoubleGenerator disallowNaN() {
        return new ConcreteDoubleGenerator(
                atomSupplier, minimum, maximum, approaching, false, allowInfinity);
    }

    public ConcreteDoubleGenerator allowInfinity() {
        return new ConcreteDoubleGenerator(
                atomSupplier, minimum, maximum, approaching, allowNaN, true);
    }

    public ConcreteDoubleGenerator disallowInfinity() {
        return new ConcreteDoubleGenerator(
                atomSupplier, minimum, maximum, approaching, allowNaN, false);
    }

    public Double gen() {
        return produce(atomSupplier.get(), atomSupplier.get(), atomSupplier.get());
    }

    private Double produce(DrawAtom atom1, DrawAtom atom2, DrawAtom atom3) {
        if (minimum > maximum)
            throw new InvalidGeneratorException(
                    String.format(
                            "Encountered a Double generator with a minimum greater than its"
                                    + " maximum. Minimum value: %f, maximum value: %f",
                            minimum, maximum));

        double result =
                switch (atom1) {
                    case Trivial1() -> 0;
                    case Trivial2() -> 1;
                    case Regular(int magnitude, boolean sign, boolean simplify) ->
                            generateFromMantissaAtoms(magnitude, atom2, atom3, sign, simplify);
                    case Edge(int magnitude, boolean sign) -> {
                        if (magnitude < oneFifthMaxInt) yield sign ? 0d : -0d;
                        if (magnitude < twoFifthsMaxInt)
                            yield sign ? Double.MIN_VALUE : -Double.MIN_VALUE;
                        if (magnitude < threeFifthsMaxInt)
                            yield sign ? Double.MAX_VALUE : -Double.MAX_VALUE;
                        if (magnitude < fourFifthsMaxInt) {
                            if (!allowInfinity)
                                yield generateFromMantissaAtoms(
                                        magnitude, atom2, atom3, sign, false);
                            yield sign ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
                        }
                        if (allowNaN) yield Float.NaN;
                        if (allowInfinity)
                            yield sign ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
                        yield generateFromMantissaAtoms(magnitude, atom2, atom3, sign, false);
                    }
                };
        if (result < minimum) return minimum;
        if (result > maximum) return maximum;
        return result;
    }

    private double generateFromMantissaAtoms(
            int exponentMagnitude, DrawAtom atom2, DrawAtom atom3, boolean sign, boolean simplify) {

        boolean effectiveSign = (minimum >= 0) || sign;
        if (maximum <= 0) effectiveSign = false;

        long signLong = effectiveSign ? 1 : 0;
        long exponent = (long) (exponentMagnitude * exponentMaxIntRatio) + 1023;
        long mantissa = getMantissa(atom2, atom3);

        long bits = signLong << 63;
        bits |= exponent << 52;
        bits |= mantissa;

        return Double.longBitsToDouble(bits);
    }

    private long getMantissa(DrawAtom atom2, DrawAtom atom3) {
        int prefix = atomToInt(atom2);
        int suffix = atomToInt(atom3);

        long unscaled = prefix;
        unscaled = unscaled << 32;
        unscaled = unscaled & suffix;

        // long unscaled = ByteBuffer.allocate(8).putInt(prefix).putInt(suffix).getLong();

        return (long) (((double) unscaled) * mantissaMaxLongRatio);
    }

    private int atomToInt(DrawAtom atom) {
        return switch (atom) {
            case Trivial1() -> 0;
            case Trivial2() -> 1;
            case Regular(int magnitude, boolean sign, boolean simplify) -> magnitude;
            case Edge(int magnitude, boolean sign) -> Integer.MAX_VALUE;
        };
    }
}
