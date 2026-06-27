package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.InvalidGeneratorException;

class DoubleProducer implements Producer<Double> {
    private final double minimum;
    private final double maximum;
    private final double approaching;
    private final boolean allowNaN;
    private final boolean allowInfinity;

    public DoubleProducer() {
        minimum = -Double.MAX_VALUE / 2f;
        maximum = Double.MAX_VALUE / 2f;
        approaching = 0f;
        allowNaN = true;
        allowInfinity = true;
    }

    public DoubleProducer(
            double minimum,
            double maximum,
            double approaching,
            boolean allowNaN,
            boolean allowInfinity) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.approaching = approaching;
        this.allowNaN = allowNaN;
        this.allowInfinity = allowInfinity;
    }

    public DoubleProducer withMinimum(double minimum) {
        return new DoubleProducer(minimum, maximum, approaching, allowNaN, allowInfinity);
    }

    public DoubleProducer withMaximum(double maximum) {
        return new DoubleProducer(minimum, maximum, approaching, allowNaN, allowInfinity);
    }

    public DoubleProducer shrinkingTowards(double approaching) {
        return new DoubleProducer(minimum, maximum, approaching, allowNaN, allowInfinity);
    }

    public DoubleProducer allowNaN() {
        return new DoubleProducer(minimum, maximum, approaching, true, allowInfinity);
    }

    public DoubleProducer disallowNaN() {
        return new DoubleProducer(minimum, maximum, approaching, false, allowInfinity);
    }

    public DoubleProducer allowInfinity() {
        return new DoubleProducer(minimum, maximum, approaching, allowNaN, true);
    }

    public DoubleProducer disallowInfinity() {
        return new DoubleProducer(minimum, maximum, approaching, allowNaN, false);
    }

    public Double produce(DrawAtom atom) {
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
