package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.InvalidGeneratorException;

class IntProducer implements Producer<Integer> {
    private final int minimum;
    private final int maximum;
    private final int approaching;

    public IntProducer() {
        minimum = Integer.MIN_VALUE;
        maximum = Integer.MAX_VALUE;
        approaching = 0;
    }

    public IntProducer(int minimum, int maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
        approaching = 0;
    }

    private IntProducer(int minimum, int maximum, int approaching) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.approaching = approaching;
    }

    public IntProducer withMinimum(int minimum) {
        return new IntProducer(minimum, maximum);
    }

    public IntProducer withMaximum(int maximum) {
        return new IntProducer(minimum, maximum);
    }

    public IntProducer shrinkingTowards(int approaching) {
        return new IntProducer(minimum, maximum, approaching);
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
                    case Edge1(_) -> minimum;
                    case Edge2(_) -> approaching - 1;
                    case Edge3(_) -> approaching;
                    case Edge4(_) -> approaching + 1;
                    case Edge5(_) -> maximum;
                },
                minimum,
                maximum);
    }
}
