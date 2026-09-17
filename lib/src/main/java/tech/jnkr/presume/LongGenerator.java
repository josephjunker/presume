package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.InvalidGeneratorException;
import tech.jnkr.presume.internal.atoms.*;

import java.util.function.Supplier;

public class LongGenerator extends AbstractGenerator<Long> implements PrimitiveGenerator<Long> {
    private final long minimum;
    private final long maximum;
    private final long approaching;
    private final Supplier<DrawAtom> atomSupplier;

    private static final int oneThirdMaxInt = Integer.MAX_VALUE / 3;
    private static final int twoThirdsMaxInt = oneThirdMaxInt * 2;

    LongGenerator(Supplier<DrawAtom> atomSupplier) {
        this.atomSupplier = atomSupplier;
        minimum = Long.MIN_VALUE;
        maximum = Long.MAX_VALUE;
        approaching = 0;
    }

    private LongGenerator(
            Supplier<DrawAtom> atomSupplier, long minimum, long maximum, long approaching) {
        this.atomSupplier = atomSupplier;
        this.minimum = minimum;
        this.maximum = maximum;
        this.approaching = approaching;
    }

    public LongGenerator withMinimumInclusive(long minimum) {
        return new LongGenerator(atomSupplier, minimum, maximum, approaching);
    }

    public LongGenerator withMaximumExclusive(long maximum) {
        return new LongGenerator(atomSupplier, minimum, maximum, approaching);
    }

    public LongGenerator shrinkingTowards(long target) {
        return new LongGenerator(atomSupplier, minimum, maximum, target);
    }

    private Long produce(DrawAtom atom1, DrawAtom atom2) {
        if (minimum > maximum)
            throw new InvalidGeneratorException(
                    String.format(
                            "Encountered a Long generator with a minimum greater than its"
                                    + " maximum. Minimum value: %d, maximum value: %d",
                            minimum, maximum));

        return Math.clamp(
                switch (atom1) {
                    case Trivial1() -> 0;
                    case Trivial2() -> 1;
                    case Regular(int magnitude, boolean sign, boolean simplify) ->
                            switch (atom2) {
                                case Trivial1(), Trivial2() -> sign ? magnitude : -magnitude;
                                case Regular(int secondMagnitude, _, _) -> {
                                    long unscaled = magnitude;
                                    unscaled = unscaled << 31;
                                    unscaled = unscaled | secondMagnitude;

                                    // TODO: this logic is wrong, I'm not accounting for the sign
                                    // bit. Also this should scale positive and negative ranges
                                    // independently, like the integer generator does.

                                    double range = maximum - minimum;
                                    double ratio = range / Long.MAX_VALUE;
                                    long result = (long) ((double) unscaled * ratio) + minimum;

                                    yield sign ? Math.abs(result) : -Math.abs(result);
                                }
                                case Edge(_, _) -> sign ? magnitude : -magnitude;
                            };
                    case Edge(int magnitude, boolean sign) -> {
                        if (magnitude < oneThirdMaxInt) yield approaching;
                        if (magnitude > twoThirdsMaxInt)
                            yield sign ? approaching - 1 : approaching + 1;
                        yield sign ? minimum : maximum;
                    }
                },
                minimum,
                maximum - 1);
    }

    @Override
    protected Long gen(GenerationSource source) {
        return produce(atomSupplier.get(), atomSupplier.get());
    }

    @Override
    public Long genPrimitive() {
        return produce(atomSupplier.get(), atomSupplier.get());
    }
}
