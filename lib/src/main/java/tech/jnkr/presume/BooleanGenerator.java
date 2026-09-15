package tech.jnkr.presume;

import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.atoms.Regular;
import tech.jnkr.presume.internal.atoms.Trivial1;
import tech.jnkr.presume.internal.atoms.Trivial2;

import java.util.function.Supplier;

public class BooleanGenerator extends AbstractGenerator<Boolean> {
    private final Supplier<DrawAtom> atomSupplier;
    private final boolean shrinkTarget;
    private static int cutoff = Integer.MAX_VALUE / 2;

    BooleanGenerator(Supplier<DrawAtom> atomSupplier) {
        this.atomSupplier = atomSupplier;
        shrinkTarget = false;
    }

    private BooleanGenerator(Supplier<DrawAtom> atomSupplier, boolean target) {
        this.atomSupplier = atomSupplier;
        shrinkTarget = target;
    }

    public BooleanGenerator shrinkTowards(boolean target) {
        return new BooleanGenerator(atomSupplier, target);
    }

    @Override
    protected Boolean gen(GenerationSource source) {
        return produce(atomSupplier.get());
    }

    private Boolean produce(DrawAtom atom) {
        return switch (atom) {
            case Trivial1(), Trivial2() -> shrinkTarget;
            case Regular(int magnitude, _, _) -> magnitude > cutoff ? !shrinkTarget : shrinkTarget;
            default -> !shrinkTarget;
        };
    }
}
