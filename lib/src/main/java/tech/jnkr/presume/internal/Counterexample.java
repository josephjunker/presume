package tech.jnkr.presume.internal;

import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.utilities.RoseTree;

import java.util.List;

public class Counterexample {
    private final RoseTree<List<DrawAtom>> state;

    public Counterexample(RoseTree<List<DrawAtom>> state) {
        this.state = state;
    }

    /*
    public Counterexample shrink(Predicate<RoseTree<List<DrawAtom>>> reproduces) {
        // This is no good, I need a zipper I think
        List<DrawAtom> rootShrinks = state.value().stream().map(atom ->
                atom.shrink().limit(10))

    }
         */
}
