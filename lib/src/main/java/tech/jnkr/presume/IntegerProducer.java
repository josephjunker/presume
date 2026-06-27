package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.InvalidGeneratorException;

class IntegerProducer implements Producer<Integer> {
    private final int minimum;
    private final int maximum;
    private final int approaching;

    public IntegerProducer() {
        minimum = Integer.MIN_VALUE;
        maximum = Integer.MAX_VALUE;
        approaching = 0;
    }

    public IntegerProducer(int minimum, int maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
        approaching = 0;
    }

    private IntegerProducer(int minimum, int maximum, int approaching) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.approaching = approaching;
    }

    public IntegerProducer withMinimum(int minimum) {
        return new IntegerProducer(minimum, maximum);
    }

    public IntegerProducer withMaximum(int maximum) {
        return new IntegerProducer(minimum, maximum);
    }

    public IntegerProducer shrinkingTowards(int approaching) {
        return new IntegerProducer(minimum, maximum, approaching);
    }

    public Integer produce(DrawAtom atom) {
        if (minimum > maximum)
            throw new InvalidGeneratorException(
                    String.format(
                            "Encountered an Integer generator with a minimum greater than its"
                                    + " maximum. Minimum value: %d, maximum value: %d",
                            minimum, maximum));

        return Math.clamp(
                switch (atom) {
                    case Trivial1(), Trivial2() -> approaching;
                    case Regular(float ratio, boolean sign, _) -> {
                        if (sign) {
                            int anchor = Math.max(approaching, minimum);
                            int positiveRange = maximum - anchor;
                            yield (int) (ratio * positiveRange) + anchor;
                        } else {
                            int anchor = Math.min(approaching, maximum);
                            int negativeRange = minimum - anchor;
                            yield (int) (ratio * negativeRange) + anchor;
                        }
                    }
                    case Edge(float ratio, boolean sign) -> {
                        if (ratio < 0.3) yield approaching;
                        if (ratio < 0.6) yield sign ? approaching - 1 : approaching + 1;
                        yield sign ? minimum : maximum;
                    }
                },
                minimum,
                maximum);
    }
}
