package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.InvalidGeneratorException;

class LongProducer implements Producer<Long> {
    private final long minimum;
    private final long maximum;
    private final long approaching;

    public LongProducer() {
        minimum = (long) Math.floor(Long.MIN_VALUE / 2f);
        maximum = (long) Math.floor(Long.MAX_VALUE / 2f);
        approaching = 0;
    }

    public LongProducer(long minimum, long maximum, long approaching) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.approaching = approaching;
    }

    public LongProducer withMinimum(long minimum) {
        return new LongProducer(minimum, maximum, approaching);
    }

    public LongProducer withMaximum(long maximum) {
        return new LongProducer(minimum, maximum, approaching);
    }

    public LongProducer shrinkingTowards(long target) {
        return new LongProducer(minimum, maximum, target);
    }

    public Long produce(DrawAtom atom) {
        if (minimum < maximum)
            throw new InvalidGeneratorException(
                    String.format(
                            "Encountered a Long generator with a minimum greater than its"
                                    + " maximum. Minimum value: %d, maximum value: %d",
                            minimum, maximum));

        return Math.clamp(
                switch (atom) {
                    case Trivial1() -> 0;
                    case Trivial2() -> 1;
                    case Regular(float ratio, boolean sign, _) -> {
                        if (sign) {
                            long anchor = Math.max(approaching, minimum);
                            long positiveRange = maximum - anchor;
                            yield (long) (ratio * positiveRange) + anchor;
                        } else {
                            long anchor = Math.min(approaching, maximum);
                            long negativeRange = minimum - anchor;
                            yield (long) (ratio * negativeRange) + anchor;
                        }
                    }
                    case Edge1(_) -> minimum;
                    case Edge2(_) -> approaching - 1;
                    case Edge3(_) -> 0;
                    case Edge4(_) -> approaching + 1;
                    case Edge5(_) -> maximum;
                },
                minimum,
                maximum);
    }
}
