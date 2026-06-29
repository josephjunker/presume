package tech.jnkr.presume.internal.generators;

import tech.jnkr.presume.generators.BooleanGenerator;
import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.atoms.Regular;
import tech.jnkr.presume.internal.atoms.Trivial1;
import tech.jnkr.presume.internal.atoms.Trivial2;

import java.util.function.Supplier;

public class ConcreteBooleanGenerator implements SimpleGenerator<Boolean>, BooleanGenerator {
    private final Supplier<DrawAtom> atomSupplier;
    private final boolean shrinkTowards;

    public ConcreteBooleanGenerator(Supplier<DrawAtom> atomSupplier) {
        this.atomSupplier = atomSupplier;
        shrinkTowards = false;
    }

    private ConcreteBooleanGenerator(Supplier<DrawAtom> atomSupplier, boolean shrinkTowards) {
        this.atomSupplier = atomSupplier;
        this.shrinkTowards = shrinkTowards;
    }

    public ConcreteBooleanGenerator shrinkTowards(boolean target) {
        return new ConcreteBooleanGenerator(atomSupplier, target);
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
