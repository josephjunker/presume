package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.InvalidGeneratorException;

import java.util.function.Supplier;

public class IntegerGenerator implements SimpleGenerator<Integer> {
    private final int minimum;
    private final int maximum;
    private final int approaching;
    private final Supplier<DrawAtom> atomSupplier;

    IntegerGenerator(Supplier<DrawAtom> atomSupplier) {
        this.atomSupplier = atomSupplier;
        minimum = Integer.MIN_VALUE;
        maximum = Integer.MAX_VALUE;
        approaching = 0;
    }

    private IntegerGenerator(
            Supplier<DrawAtom> atomSupplier, int minimum, int maximum, int approaching) {
        this.atomSupplier = atomSupplier;
        this.minimum = minimum;
        this.maximum = maximum;
        this.approaching = approaching;
    }

    public IntegerGenerator withMinimum(int minimum) {
        return new IntegerGenerator(atomSupplier, minimum, maximum, approaching);
    }

    public IntegerGenerator withMaximum(int maximum) {
        return new IntegerGenerator(atomSupplier, minimum, maximum, approaching);
    }

    public IntegerGenerator shrinkingTowards(int approaching) {
        return new IntegerGenerator(atomSupplier, minimum, maximum, approaching);
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
                    case Trivial1() -> approaching;
                    case Trivial2() -> approaching;
                    case Regular(float ratio, boolean sign, boolean simplify) -> {
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

    public Integer gen() {
        return produce(atomSupplier.get());
    }
}
