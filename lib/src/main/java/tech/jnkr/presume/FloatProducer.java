package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.InvalidGeneratorException;

class FloatProducer implements Producer<Float> {
    private final float minimum;
    private final float maximum;
    private final float approaching;
    private final boolean allowNaN;
    private final boolean allowInfinity;

    public FloatProducer() {
        minimum = -Float.MAX_VALUE / 2f;
        maximum = Float.MAX_VALUE / 2f;
        approaching = 0f;
        allowNaN = true;
        allowInfinity = true;
    }

    public FloatProducer(
            float minimum,
            float maximum,
            float approaching,
            boolean allowNaN,
            boolean allowInfinity) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.approaching = approaching;
        this.allowNaN = allowNaN;
        this.allowInfinity = allowInfinity;
    }

    public FloatProducer withMinimum(float minimum) {
        return new FloatProducer(minimum, maximum, approaching, allowNaN, allowInfinity);
    }

    public FloatProducer withMaximum(float maximum) {
        return new FloatProducer(minimum, maximum, approaching, allowNaN, allowInfinity);
    }

    public FloatProducer shrinkingTowards(float approaching) {
        return new FloatProducer(minimum, maximum, approaching, allowNaN, allowInfinity);
    }

    public FloatProducer allowNaN() {
        return new FloatProducer(minimum, maximum, approaching, true, allowInfinity);
    }

    public FloatProducer disallowNaN() {
        return new FloatProducer(minimum, maximum, approaching, false, allowInfinity);
    }

    public FloatProducer allowInfinity() {
        return new FloatProducer(minimum, maximum, approaching, allowNaN, true);
    }

    public FloatProducer disallowInfinity() {
        return new FloatProducer(minimum, maximum, approaching, allowNaN, false);
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
                    case Trivial1() -> 0f;
                    case Trivial2() -> 1f;
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

    private float generateFromRatio(float ratio, boolean sign, boolean simplify) {
        if (sign) {
            float anchor = Math.max(approaching, minimum);
            float positiveRange = maximum - anchor;
            float value = (ratio * positiveRange) + anchor;
            return simplify ? (float) Math.floor(value) : value;
        } else {
            float anchor = Math.min(approaching, maximum);
            float negativeRange = minimum - anchor;
            float value = (ratio * negativeRange) + anchor;
            return simplify ? (float) Math.floor(value) : value;
        }
    }
}
