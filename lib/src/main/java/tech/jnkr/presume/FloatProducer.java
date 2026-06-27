package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.InvalidGeneratorException;

class FloatProducer implements Producer<Float> {
    private final float minimum;
    private final float maximum;
    private final float approaching;

    public FloatProducer() {
        minimum = -Float.MAX_VALUE / 2f;
        maximum = Float.MAX_VALUE / 2f;
        approaching = 0f;
    }

    public FloatProducer(float minimum, float maximum, float approaching) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.approaching = approaching;
    }

    public FloatProducer withMinimum(float minimum) {
        return new FloatProducer(minimum, maximum, approaching);
    }

    public FloatProducer withMaximum(float maximum) {
        return new FloatProducer(minimum, maximum, approaching);
    }

    public FloatProducer shrinkingTowards(float approaching) {
        return new FloatProducer(minimum, maximum, approaching);
    }

    public Float produce(DrawAtom atom) {
        if (minimum < maximum)
            throw new InvalidGeneratorException(
                    String.format(
                            "Encountered a Float generator with a minimum greater than its"
                                    + " maximum. Minimum value: %f, maximum value: %f",
                            minimum, maximum));

        float result =
                switch (atom) {
                    case Trivial1() -> 0;
                    case Trivial2() -> 1;
                    case Regular(float ratio, boolean sign, boolean simplify) -> {
                        if (sign) {
                            float anchor = Math.max(approaching, minimum);
                            float positiveRange = maximum - anchor;
                            float value = (ratio * positiveRange) + anchor;
                            yield simplify ? (float) Math.floor(value) : value;
                        } else {
                            float anchor = Math.min(approaching, maximum);
                            float negativeRange = minimum - anchor;
                            float value = (ratio * negativeRange) + anchor;
                            yield simplify ? (float) Math.floor(value) : value;
                        }
                    }
                    case Edge1(_) -> -0f;
                    case Edge2(_) -> Float.POSITIVE_INFINITY;
                    case Edge3(_) -> Float.NEGATIVE_INFINITY;
                    case Edge4(_) -> Float.NaN;
                    case Edge5(_) -> Float.MIN_VALUE;
                };
        if (result < minimum) return minimum;
        if (result > maximum) return maximum;
        return result;
    }
}
