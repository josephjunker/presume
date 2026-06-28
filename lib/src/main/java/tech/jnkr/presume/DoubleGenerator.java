package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.InvalidGeneratorException;

import java.util.function.Supplier;

public class DoubleGenerator implements SimpleGenerator<Double> {
    private final double minimum;
    private final double maximum;
    private final double approaching;
    private final boolean allowNaN;
    private final boolean allowInfinity;
    private final Supplier<DrawAtom> atomSupplier;

    DoubleGenerator(Supplier<DrawAtom> atomSupplier) {
        this.atomSupplier = atomSupplier;
        minimum = -Double.MAX_VALUE / 2f;
        maximum = Double.MAX_VALUE / 2f;
        approaching = 0f;
        allowNaN = true;
        allowInfinity = true;
    }

    private DoubleGenerator(
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

    public DoubleGenerator withMinimum(double minimum) {
        return new DoubleGenerator(
                atomSupplier, minimum, maximum, approaching, allowNaN, allowInfinity);
    }

    public DoubleGenerator withMaximum(double maximum) {
        return new DoubleGenerator(
                atomSupplier, minimum, maximum, approaching, allowNaN, allowInfinity);
    }

    public DoubleGenerator shrinkingTowards(double approaching) {
        return new DoubleGenerator(
                atomSupplier, minimum, maximum, approaching, allowNaN, allowInfinity);
    }

    public DoubleGenerator allowNaN() {
        return new DoubleGenerator(atomSupplier, minimum, maximum, approaching, true, allowInfinity);
    }

    public DoubleGenerator disallowNaN() {
        return new DoubleGenerator(
                atomSupplier, minimum, maximum, approaching, false, allowInfinity);
    }

    public DoubleGenerator allowInfinity() {
        return new DoubleGenerator(atomSupplier, minimum, maximum, approaching, allowNaN, true);
    }

    public DoubleGenerator disallowInfinity() {
        return new DoubleGenerator(atomSupplier, minimum, maximum, approaching, allowNaN, false);
    }

    public Double gen() {
        return produce(atomSupplier.get());
    }

    private Double produce(DrawAtom atom) {
        if (minimum < maximum)
            throw new InvalidGeneratorException(
                    String.format(
                            "Encountered a Double generator with a minimum greater than its"
                                    + " maximum. Minimum value: %f, maximum value: %f",
                            minimum, maximum));

        double result =
                switch (atom) {
                    case Trivial1() -> 0;
                    case Trivial2() -> 1;
                    case Regular(float ratio, boolean sign, boolean simplify) ->
                            this.generateFromRatio(ratio, sign, simplify);
                    case Edge(float ratio, boolean sign) -> {
                        if (ratio < 0.2f) yield sign ? 0f : -0f;
                        if (ratio < 0.4f) yield sign ? Float.MIN_VALUE : -Float.MIN_VALUE;
                        if (ratio < 0.6f) yield sign ? approaching + 1 : approaching - 1;
                        if (ratio < 0.8f) {
                            if (!allowInfinity) yield this.generateFromRatio(ratio, sign, false);
                            yield sign ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
                        }
                        if (allowNaN) yield Float.NaN;
                        if (allowInfinity) yield Float.POSITIVE_INFINITY;
                        yield generateFromRatio(ratio, sign, false);
                    }
                };
        if (result < minimum) return minimum;
        if (result > maximum) return maximum;
        return result;
    }

    private double generateFromRatio(float ratio, boolean sign, boolean simplify) {
        if (sign) {
            double anchor = Math.max(approaching, minimum);
            double positiveRange = maximum - anchor;
            double value = (ratio * positiveRange) + anchor;
            return simplify ? Math.floor(value) : value;
        } else {
            double anchor = Math.min(approaching, maximum);
            double negativeRange = minimum - anchor;
            double value = (ratio * negativeRange) + anchor;
            return simplify ? Math.floor(value) : value;
        }
    }
}
