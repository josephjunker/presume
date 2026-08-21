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

        int result =
                Math.clamp(
                        switch (atom) {
                            case Trivial1() -> approaching;
                            case Trivial2() -> approaching;
                            case Regular(double ratio, boolean sign, boolean simplify) -> {
                                if (minimum >= 0) {
                                    yield generatePositive(ratio);
                                } else if (maximum <= 0) {
                                    yield generateNegative(ratio);
                                } else if (sign) {
                                    yield generatePositive(ratio);
                                } else {
                                    yield generateNegative(ratio);
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

        // System.out.println(result);
        // System.out.println();
        return result;
    }

    private int generatePositive(double ratio) {
        int anchor = Math.max(approaching, minimum);
        int positiveRange = maximum - anchor;
        return Math.round((float) ratio * positiveRange) + anchor;
    }

    private int generateNegative(double ratio) {
        int anchor = Math.min(approaching, maximum);
        int negativeRange = minimum - anchor;
        return Math.round((float) ratio * negativeRange) + anchor;
    }
}
