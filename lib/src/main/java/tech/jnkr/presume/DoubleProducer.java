package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.InvalidGeneratorException;

class DoubleProducer implements Producer<Double> {
    private final double minimum;
    private final double maximum;
    private final double approaching;

    public DoubleProducer() {
        minimum = -Double.MAX_VALUE / 2f;
        maximum = Double.MAX_VALUE / 2f;
        approaching = 0f;
    }

    public DoubleProducer(double minimum, double maximum, double approaching) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.approaching = approaching;
    }

    public DoubleProducer withMinimum(double minimum) {
        return new DoubleProducer(minimum, maximum, approaching);
    }

    public DoubleProducer withMaximum(double maximum) {
        return new DoubleProducer(minimum, maximum, approaching);
    }

    public DoubleProducer shrinkingTowards(double approaching) {
        return new DoubleProducer(minimum, maximum, approaching);
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
                    case Regular(float ratio, boolean sign, boolean simplify) -> {
                        if (sign) {
                            double anchor = Math.max(approaching, minimum);
                            double positiveRange = maximum - anchor;
                            double value = (ratio * positiveRange) + anchor;
                            yield simplify ? Math.floor(value) : value;
                        } else {
                            double anchor = Math.min(approaching, maximum);
                            double negativeRange = minimum - anchor;
                            double value = (ratio * negativeRange) + anchor;
                            yield simplify ? Math.floor(value) : value;
                        }
                    }
                    case Edge1(_) -> -0f;
                    case Edge2(_) -> Double.POSITIVE_INFINITY;
                    case Edge3(_) -> Double.NEGATIVE_INFINITY;
                    case Edge4(_) -> Double.NaN;
                    case Edge5(_) -> Double.MIN_VALUE;
                };
        if (result < minimum) return minimum;
        if (result > maximum) return maximum;
        return result;
    }
}
