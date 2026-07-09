package tech.jnkr.presume.internal;

import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.shrinking.Down;
import tech.jnkr.presume.internal.shrinking.Right;
import tech.jnkr.presume.internal.shrinking.TraceEntry;
import tech.jnkr.presume.internal.utilities.*;

import java.util.ArrayList;
import java.util.function.Function;

public class HistoryZipper {
    private final RoseTreeZipper<Maybe<ListZipper<TraceEntry>>> composedZipper;
    private final int childIndex;

    private HistoryZipper(
            RoseTreeZipper<Maybe<ListZipper<TraceEntry>>> composedZipper, int childIndex) {
        this.composedZipper = composedZipper;
        this.childIndex = childIndex;
    }

    public static HistoryZipper fromTrace(RoseTree<ImmutableList<TraceEntry>> trace) {
        return new HistoryZipper(RoseTreeZipper.fromTree(trace.map(ImmutableList::toZipper)), 0);
    }

    public Maybe<HistoryZipper> right() {
        var inner = composedZipper.value().chain(ListZipper::right);

        return inner.map(
                listZipper ->
                        new HistoryZipper(
                                composedZipper.replace(Maybe.of(listZipper)), childIndex));
    }

    public Maybe<HistoryZipper> down() {
        var maybeZipper = composedZipper.nthChild(childIndex);

        return maybeZipper.map(roseZipper -> new HistoryZipper(roseZipper, childIndex));
    }

    // Note that this moves us both up AND right!
    // This is because we don't want to re-enter a subtree after we've already used it during
    // shrinking
    public Maybe<HistoryZipper> exitSubtree() {
        var maybeZipper = composedZipper.up();

        return maybeZipper.map(roseZipper -> new HistoryZipper(roseZipper, childIndex + 1));
    }

    /*
    Invariant: the result of this, if it is a Just, always has its focus pointing to
    a Right(DrawAtom), not to a Down()
    */
    public Maybe<HistoryZipper> chaseToAtomIndex(int index) {
        var result = Maybe.of(this);
        int i = 0;

        while (i < index) {

            switch (result) {
                case Nothing():
                    return new Nothing<>();
                case Just(var historyZipper):
                    {
                        switch (historyZipper.composedZipper.value()) {
                            // This occurs if we have descended into a generator which
                            // never
                            // requested a DrawAtom, resulting in the zipper for this
                            // generator
                            // being empty, or if we have already used up all of the
                            // atoms
                            // for the current generator (i.e. moved all the way to the
                            // right)
                            case Nothing():
                                {
                                    result = historyZipper.exitSubtree();
                                    continue;
                                }
                            // We are in a generator which still has DrawAtoms remaining
                            case Just(ListZipper<TraceEntry> listZipper):
                                {
                                    switch (listZipper.focus()) {
                                        // We always chase Down immediately, without incrementing
                                        // i, because `index` is the count of actual *atoms* that
                                        // we've passed, not the count of TraceEntries.
                                        case Down() -> result = historyZipper.down();
                                        case Right(DrawAtom atom) -> {
                                            i = i + 1;
                                            result = historyZipper.right();
                                        }
                                    }
                                }
                        }
                    }
            }
        }

        return result;
    }

    public RoseTree<ArrayList<DrawAtom>> toHistory() {
        return composedZipper
                .toTree()
                .map(
                        maybeZipper -> {
                            ImmutableList<TraceEntry> entries =
                                    maybeZipper
                                            .map(ListZipper::toList)
                                            .orDefault(ImmutableList.empty());

                            ImmutableList<DrawAtom> atoms =
                                    entries.filterMap(
                                            entry ->
                                                    switch (entry) {
                                                        case Down() -> new Nothing<DrawAtom>();
                                                        case Right(DrawAtom atom) -> Maybe.of(atom);
                                                    });

                            return atoms.toArrayList();
                        });
    }

    public Maybe<DrawAtom> focus() {
        // We want to make sure we're pointing at a Right(), if any Rights remain in the tree.
        Maybe<HistoryZipper> normalized = chaseToAtomIndex(0);

        return normalized.chain(
                historyZipper ->
                        composedZipper
                                .value()
                                .map(
                                        listZipper -> {
                                            TraceEntry focus = listZipper.focus();

                                            // Because of the invariant enforced by
                                            // chaseToAtomIndex, we know we're pointing at a Right.
                                            return ((Right) focus).atom();
                                        }));
    }

    public Maybe<HistoryZipper> replace(DrawAtom atom) {
        // normalized will point to a Right, if it exists
        Maybe<HistoryZipper> normalized = chaseToAtomIndex(0);

        // This will always be a Just, because of chaseToAtomIndex's invariant
        Function<Maybe<ListZipper<TraceEntry>>, Maybe<ListZipper<TraceEntry>>> updateInnerZipper =
                (maybeListZipper ->
                        maybeListZipper.map(listZipper -> listZipper.replace(new Right(atom))));

        return normalized.map(
                historyZipper ->
                        new HistoryZipper(
                                historyZipper.composedZipper.update(updateInnerZipper),
                                childIndex));
    }
}
