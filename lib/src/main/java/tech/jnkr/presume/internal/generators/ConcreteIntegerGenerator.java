package tech.jnkr.presume.internal.generators;

import tech.jnkr.presume.exceptions.InvalidGeneratorException;
import tech.jnkr.presume.internal.atoms.*;

import java.util.function.Supplier;

public class ConcreteIntegerGenerator
        implements SimpleGenerator<Integer>, tech.jnkr.presume.generators.IntegerGenerator {
    private final int minimum;
    private final int maximum;
    private final int approaching;
    private final Supplier<DrawAtom> atomSupplier;

    private static final int oneThirdMax = Integer.MAX_VALUE / 3;
    private static final int twoThirdsMax = oneThirdMax * 2;

    public ConcreteIntegerGenerator(Supplier<DrawAtom> atomSupplier) {
        this.atomSupplier = atomSupplier;
        minimum = Integer.MIN_VALUE;
        maximum = Integer.MAX_VALUE;
        approaching = 0;
    }

    private ConcreteIntegerGenerator(
            Supplier<DrawAtom> atomSupplier, int minimum, int maximum, int approaching) {
        this.atomSupplier = atomSupplier;
        this.minimum = minimum;
        this.maximum = maximum;
        this.approaching = approaching;
    }

    @Override
    public ConcreteIntegerGenerator withMinimum(int minimum) {
        return new ConcreteIntegerGenerator(atomSupplier, minimum, maximum, approaching);
    }

    @Override
    public ConcreteIntegerGenerator withMaximum(int maximum) {
        return new ConcreteIntegerGenerator(atomSupplier, minimum, maximum, approaching);
    }

    @Override
    public ConcreteIntegerGenerator shrinkingTowards(int approaching) {
        return new ConcreteIntegerGenerator(atomSupplier, minimum, maximum, approaching);
    }

    @Override
    public Integer gen() {
        return produce(atomSupplier.get());
    }

    private Integer produce(DrawAtom atom) {
        if (minimum > maximum)
            throw new InvalidGeneratorException(
                    String.format(
                            "Encountered an Integer generator with a minimum greater than its"
                                    + " maximum. Minimum value: %d, maximum value: %d",
                            minimum, maximum));

        return Math.clamp(
                switch (atom) {
                    case Trivial1(), Trivial2() -> approaching;
                    case Regular(int magnitude, boolean sign, boolean simplify) -> {
                        if (minimum >= 0) {
                            yield minimum + scale(magnitude);
                        } else if (maximum <= 0) {
                            yield maximum - scale(magnitude);
                        } else if (sign) {
                            yield scale(magnitude);
                        } else {
                            yield -scale(magnitude);
                        }
                    }
                    case Edge(int magnitude, boolean sign) -> {
                        if (magnitude < oneThirdMax) yield approaching;
                        if (magnitude < twoThirdsMax)
                            yield sign ? approaching - 1 : approaching + 1;
                        yield sign ? minimum : maximum;
                    }
                },
                minimum,
                maximum);
    }

    private int scale(int magnitude) {
        int range = maximum - minimum;
        double ratio = (double) range / Integer.MAX_VALUE;
        return (int) (magnitude * ratio);
    }
}
