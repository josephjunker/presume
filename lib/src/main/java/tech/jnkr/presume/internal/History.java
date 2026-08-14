package tech.jnkr.presume.internal;

import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.utilities.RoseTree;

import java.util.ArrayList;
import java.util.Objects;

public class History {
    public RoseTree<ArrayList<DrawAtom>> contents;

    public History(RoseTree<ArrayList<DrawAtom>> contents) {
        this.contents = contents;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof History history)) return false;
        return Objects.equals(contents, history.contents);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(contents);
    }
}
