package tech.jnkr.presume.internal.generators;

import tech.jnkr.presume.exceptions.InvalidGeneratorException;
import tech.jnkr.presume.generators.LongGenerator;
import tech.jnkr.presume.internal.atoms.*;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

public class ConcreteLongGenerator implements SimpleGenerator<Long>, LongGenerator {
    private final long minimum;
    private final long maximum;
    private final long approaching;
    private final Supplier<DrawAtom> atomSupplier;

    private static final int oneThirdMaxInt = Integer.MAX_VALUE / 3;
    private static final int twoThirdsMaxInt = oneThirdMaxInt * 2;

    public ConcreteLongGenerator(Supplier<DrawAtom> atomSupplier) {
        this.atomSupplier = atomSupplier;
        minimum = Long.MIN_VALUE;
        maximum = Long.MAX_VALUE;
        approaching = 0;
    }

    private ConcreteLongGenerator(
            Supplier<DrawAtom> atomSupplier, long minimum, long maximum, long approaching) {
        this.atomSupplier = atomSupplier;
        this.minimum = minimum;
        this.maximum = maximum;
        this.approaching = approaching;
    }

    @Override
    public ConcreteLongGenerator withMinimum(long minimum) {
        return new ConcreteLongGenerator(atomSupplier, minimum, maximum, approaching);
    }

    @Override
    public ConcreteLongGenerator withMaximum(long maximum) {
        return new ConcreteLongGenerator(atomSupplier, minimum, maximum, approaching);
    }

    @Override
    public ConcreteLongGenerator shrinkingTowards(long target) {
        return new ConcreteLongGenerator(atomSupplier, minimum, maximum, target);
    }

    @Override
    public Long gen() {
        return produce(atomSupplier.get(), atomSupplier.get());
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
                                case Trivial1() -> sign ? magnitude : -magnitude;
                                case Trivial2() -> sign ? magnitude : -magnitude;
                                case Regular(
                                                int secondMagnitude,
                                                boolean secondSign,
                                                boolean secondSimplify) -> {
                                    long result =
                                            ByteBuffer.allocate(8)
                                                    .putInt(magnitude)
                                                    .putInt(secondMagnitude)
                                                    .getLong();

                                    // TODO: scale to be between maximum and minimum
                                    yield sign ? result : -result;
                                }
                                case Edge(int secondMagnitude, boolean secondSign) ->
                                        sign ? magnitude : -magnitude;
                            };
                    case Edge(int magnitude, boolean sign) -> {
                        if (magnitude < oneThirdMaxInt) yield approaching;
                        if (magnitude > twoThirdsMaxInt)
                            yield sign ? approaching - 1 : approaching + 1;
                        yield sign ? minimum : maximum;
                    }
                },
                minimum,
                maximum);
    }
}
