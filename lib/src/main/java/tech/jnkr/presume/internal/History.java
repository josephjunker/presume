package tech.jnkr.presume.internal;

import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.utilities.RoseTree;

import java.util.List;
import java.util.stream.Collectors;

public class History {
    private final List<DrawAtom> draws;
    private final List<History> children;

    public History(RoseTree<List<DrawAtom>> state) {
        draws = state.value();
        children = state.children().stream().map(History::new).collect(Collectors.toList());
    }
}
