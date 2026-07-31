package tech.jnkr.presume.internal;

import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.shrinking.Down;
import tech.jnkr.presume.internal.shrinking.Right;
import tech.jnkr.presume.internal.shrinking.TraceEntry;
import tech.jnkr.presume.internal.utilities.ImmutableList;
import tech.jnkr.presume.internal.utilities.Maybe;
import tech.jnkr.presume.internal.utilities.RoseTree;

import java.util.ArrayList;

public class Trace {
    public final RoseTree<ImmutableList<TraceEntry>> contents;

    public Trace(RoseTree<ImmutableList<TraceEntry>> contents) {
        this.contents = contents;
    }

    public History toHistory() {
        RoseTree<ArrayList<DrawAtom>> historyContents =
                contents.map(
                        list ->
                                list.filterMap(
                                                entry ->
                                                        switch (entry) {
                                                            case Right(DrawAtom atom) ->
                                                                    Maybe.of(atom);
                                                            case Down() -> Maybe.empty();
                                                        })
                                        .toArrayList());

        return new History(historyContents);
    }

    public int atomCount() {
        return contents.foldDepthFirst(
                (Integer total, ImmutableList<TraceEntry> list) ->
                        list.foldLeft(
                                (Integer acc, TraceEntry entry) ->
                                        switch (entry) {
                                            case Right(var ignored) -> acc + 1;
                                            case Down() -> acc;
                                        },
                                total),
                0);
    }
}
