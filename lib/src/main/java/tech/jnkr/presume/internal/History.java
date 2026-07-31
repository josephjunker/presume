package tech.jnkr.presume.internal;

import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.utilities.RoseTree;

import java.util.ArrayList;

public class History {
    public RoseTree<ArrayList<DrawAtom>> contents;

    public History(RoseTree<ArrayList<DrawAtom>> contents) {
        this.contents = contents;
    }
}
