package tech.jnkr.presume;

import java.util.function.Supplier;

public class BooleanProducer implements Producer<Boolean> {
    private final Supplier<DrawAtom> atomSupplier;
    private final boolean shrinkTowards;

    BooleanProducer(Supplier<DrawAtom> atomSupplier) {
        this.atomSupplier = atomSupplier;
        shrinkTowards = false;
    }

    private BooleanProducer(Supplier<DrawAtom> atomSupplier, boolean shrinkTowards) {
        this.atomSupplier = atomSupplier;
        this.shrinkTowards = shrinkTowards;
    }

    public BooleanProducer shrinkTowards(boolean target) {
        return new BooleanProducer(atomSupplier, target);
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
