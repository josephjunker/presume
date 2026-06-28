package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.InvalidGeneratorException;

import java.util.function.Supplier;

public class LongGenerator implements SimpleGenerator<Long> {
    private final long minimum;
    private final long maximum;
    private final long approaching;
    private final Supplier<DrawAtom> atomSupplier;

    LongGenerator(Supplier<DrawAtom> atomSupplier) {
        this.atomSupplier = atomSupplier;
        minimum = (long) Math.floor(Long.MIN_VALUE / 2f);
        maximum = (long) Math.floor(Long.MAX_VALUE / 2f);
        approaching = 0;
    }

    private LongGenerator(
            Supplier<DrawAtom> atomSupplier, long minimum, long maximum, long approaching) {
        this.atomSupplier = atomSupplier;
        this.minimum = minimum;
        this.maximum = maximum;
        this.approaching = approaching;
    }

    public LongGenerator withMinimum(long minimum) {
        return new LongGenerator(atomSupplier, minimum, maximum, approaching);
    }

    public LongGenerator withMaximum(long maximum) {
        return new LongGenerator(atomSupplier, minimum, maximum, approaching);
    }

    public LongGenerator shrinkingTowards(long target) {
        return new LongGenerator(atomSupplier, minimum, maximum, target);
    }

    public Long gen() {
        return produce(atomSupplier.get());
    }

    private Long produce(DrawAtom atom) {
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
                    case Regular(float ratio, boolean sign, boolean simplify) -> {
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
