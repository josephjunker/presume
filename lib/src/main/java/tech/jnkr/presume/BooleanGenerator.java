package tech.jnkr.presume;

import java.util.function.Supplier;

public class BooleanGenerator implements SimpleGenerator<Boolean> {
    private final Supplier<DrawAtom> atomSupplier;
    private final boolean shrinkTowards;

    BooleanGenerator(Supplier<DrawAtom> atomSupplier) {
        this.atomSupplier = atomSupplier;
        shrinkTowards = false;
    }

    private BooleanGenerator(Supplier<DrawAtom> atomSupplier, boolean shrinkTowards) {
        this.atomSupplier = atomSupplier;
        this.shrinkTowards = shrinkTowards;
    }

    public BooleanGenerator shrinkTowards(boolean target) {
        return new BooleanGenerator(atomSupplier, target);
    }

    public Boolean gen() {
        return produce(atomSupplier.get());
    }

    private Boolean produce(DrawAtom atom) {
        return switch (atom) {
            case Trivial1() -> false;
            case Trivial2() -> false;
            case Regular(float ratio, boolean sign, boolean simplify) -> ratio < 0.5;
            default -> true;
        };
    }
}
