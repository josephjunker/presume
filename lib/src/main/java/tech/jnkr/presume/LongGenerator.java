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

    // The concatenation of two non-negative int magnitudes spans [0, 2^62).
    private static final double UNSIGNED_MAGNITUDE_RANGE = (double) (1L << 62);

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

        // Note: the switch is hoisted out of the Math.clamp call because passing a switch
        // expression as an overloaded method argument crashes javac 21.
        long unclamped =
                switch (atom1) {
                    case Trivial1 ignoredT1 -> approaching;
                    case Trivial2 ignoredT2 -> approaching;
                    case Regular(int magnitude, boolean sign, boolean ignoredSimplify) ->
                            switch (atom2) {
                                case Trivial1 ignoredT1 -> sign ? magnitude : -magnitude;
                                case Trivial2 ignoredT2 -> sign ? magnitude : -magnitude;
                                case Regular(
                                                int secondMagnitude,
                                                boolean ignoredSecondSign,
                                                boolean ignoredSecondSimplify) -> {
                                    long unscaled = ((long) magnitude << 31) | secondMagnitude;

                                    if (minimum >= 0) {
                                        yield minimum + scale(unscaled, maximum - minimum);
                                    } else if (maximum <= 0) {
                                        yield maximum
                                                - scale(
                                                        unscaled,
                                                        (double) maximum - (double) minimum);
                                    } else if (sign) {
                                        yield scale(unscaled, maximum);
                                    } else {
                                        yield -scale(unscaled, -(double) minimum);
                                    }
                                }
                                case Edge(int ignoredEdgeMagnitude, boolean ignoredEdgeSign) ->
                                        sign ? magnitude : -magnitude;
                            };
                    case Edge(int magnitude, boolean sign) -> {
                        if (magnitude < oneThirdMaxInt) yield approaching;
                        if (magnitude < twoThirdsMaxInt)
                            yield sign ? approaching - 1 : approaching + 1;
                        yield sign ? minimum : maximum;
                    }
                };
        return Math.clamp(unclamped, minimum, maximum - 1);
    }

    private long scale(long magnitude, double range) {
        double ratio = (double) magnitude / UNSIGNED_MAGNITUDE_RANGE;
        return (long) (ratio * range);
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
