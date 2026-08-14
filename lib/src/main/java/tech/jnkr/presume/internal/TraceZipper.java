package tech.jnkr.presume.internal;

import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.shrinking.Down;
import tech.jnkr.presume.internal.shrinking.Right;
import tech.jnkr.presume.internal.shrinking.TraceEntry;
import tech.jnkr.presume.internal.utilities.*;

import java.util.function.Function;

public class TraceZipper {
    private final RoseTreeZipper<Maybe<ListZipper<TraceEntry>>> composedZipper;
    private final int childIndex;

    private TraceZipper(
            RoseTreeZipper<Maybe<ListZipper<TraceEntry>>> composedZipper, int childIndex) {
        this.composedZipper = composedZipper;
        this.childIndex = childIndex;
    }

    public static TraceZipper fromTrace(Trace trace) {
        return new TraceZipper(
                RoseTreeZipper.fromTree(trace.contents.map(ImmutableList::toZipper)), 0);
    }

    public Maybe<TraceZipper> right() {
        var inner = composedZipper.value().chain(ListZipper::right);

        return inner.map(
                listZipper ->
                        new TraceZipper(composedZipper.replace(Maybe.of(listZipper)), childIndex));
    }

    public Maybe<TraceZipper> down() {
        var maybeZipper = composedZipper.nthChild(childIndex);

        return maybeZipper.map(roseZipper -> new TraceZipper(roseZipper, childIndex));
    }

    // Note that this moves us both up AND right!
    // This is because we don't want to re-enter a subtree after we've already used it during
    // shrinking
    public Maybe<TraceZipper> exitSubtree() {
        var maybeZipper = composedZipper.up();

        return maybeZipper.map(roseZipper -> new TraceZipper(roseZipper, childIndex + 1));
    }

    private Maybe<TraceZipper> chaseToNextAtom() {
        var result = Maybe.of(this);

        while (true) {
            switch (result) {
                case Nothing():
                    return new Nothing<>();
                case Just(var traceZipper):
                    {
                        switch (traceZipper.composedZipper.value()) {
                            // This occurs if we have descended into a generator which
                            // never requested a DrawAtom, resulting in the zipper for this
                            // generator being empty, or if we have already used up all of the
                            // atoms for the current generator (i.e. moved all the way to the
                            // right)
                            case Nothing():
                                {
                                    result = traceZipper.exitSubtree();
                                    continue;
                                }
                            // We are in a generator which still has DrawAtoms remaining
                            case Just(ListZipper<TraceEntry> listZipper):
                                {
                                    switch (listZipper.focus()) {
                                        case Down():
                                            {
                                                result = traceZipper.down();
                                                break;
                                            }
                                        case Right(DrawAtom ignored):
                                            {
                                                // We've finally reached a value; we can return
                                                return result;
                                            }
                                    }
                                }
                        }
                    }
            }
        }
    }

    /*
    Invariant: the result of this, if it is a Just, always has its focus pointing to
    a Right(DrawAtom), not to a Down()
    */
    public Maybe<TraceZipper> chaseToAtomIndex(int index) {

        var result = chaseToNextAtom();
        int i = 0;
        while (i < index) {
            switch (result) {
                case Nothing():
                    return new Nothing<>();
                case Just(var traceZipper):
                    result = traceZipper.chaseToNextAtom();
            }
            i++;
        }
        return result;
    }

    public Trace toTrace() {
        return new Trace(
                composedZipper
                        .toTree()
                        .map(
                                maybeZipper ->
                                        maybeZipper
                                                .map(ListZipper::toList)
                                                .orDefault(ImmutableList.empty())));
    }

    public Maybe<DrawAtom> focus() {
        // We want to make sure we're pointing at a Right(), if any Rights remain in the tree.
        Maybe<TraceZipper> normalized = chaseToNextAtom();

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

    public Maybe<TraceZipper> replace(DrawAtom atom) {
        // normalized will point to a Right, if it exists
        Maybe<TraceZipper> normalized = chaseToNextAtom();

        // This will always be a Just, because of chaseToNextAtom's invariant
        Function<Maybe<ListZipper<TraceEntry>>, Maybe<ListZipper<TraceEntry>>> updateInnerZipper =
                (maybeListZipper ->
                        maybeListZipper.map(listZipper -> listZipper.replace(new Right(atom))));

        return normalized.map(
                historyZipper ->
                        new TraceZipper(
                                historyZipper.composedZipper.update(updateInnerZipper),
                                childIndex));
    }
}
