package tech.jnkr.presume.internal;

import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.utilities.ImmutableList;
import tech.jnkr.presume.internal.utilities.RoseTree;

public class History {
    public final ImmutableList<DrawAtom> draws;
    public final ImmutableList<History> children;

    public History(RoseTree<ImmutableList<DrawAtom>> state) {
        draws = state.value;
        children = state.children.map(History::new);
    }
}
