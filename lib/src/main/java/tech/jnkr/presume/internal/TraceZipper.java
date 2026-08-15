package tech.jnkr.presume.internal;

import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.shrinking.Down;
import tech.jnkr.presume.internal.shrinking.Right;
import tech.jnkr.presume.internal.shrinking.TraceEntry;
import tech.jnkr.presume.internal.utilities.*;

import java.util.Objects;
import java.util.function.Function;

public class TraceZipper {
    private final RoseTreeZipper<Tuple<Integer, Maybe<ListZipper<TraceEntry>>>> composedZipper;

    private TraceZipper(
            RoseTreeZipper<Tuple<Integer, Maybe<ListZipper<TraceEntry>>>> composedZipper) {
        this.composedZipper = composedZipper;
    }

    public static TraceZipper fromTrace(Trace trace) {
        return new TraceZipper(
                RoseTreeZipper.fromTree(
                        trace.contents.map(list -> new Tuple<>(0, list.toZipper()))));
    }

    public Maybe<TraceZipper> right() {
        Tuple<Integer, Maybe<ListZipper<TraceEntry>>> inner = composedZipper.value();

        Maybe<ListZipper<TraceEntry>> newInner = inner.second().chain(ListZipper::right);

        return newInner.isJust()
                ? new Just<>(
                        new TraceZipper(
                                composedZipper.replace(new Tuple<>(inner.first(), newInner))))
                : new Nothing<>();
    }

    public Maybe<TraceZipper> down() {
        return composedZipper.nthChild(composedZipper.value().first()).map(TraceZipper::new);
    }

    // Note that this moves us both up AND right!
    // This is because we don't want to re-enter a subtree after we've already used it during
    // shrinking.
    // It can move us up more than one level, because that's just how I can make the types work out.
    public Maybe<TraceZipper> chaseExitSubtree() {

        var parent = composedZipper.up();

        return switch (parent) {
            case Nothing() -> new Nothing<>();
            case Just(var treeZipper) -> {
                int newChildIndex = treeZipper.value().first() + 1;
                var rightZipper = treeZipper.value().second().chain(ListZipper::right);

                yield switch (rightZipper) {
                    case Nothing() -> new TraceZipper(treeZipper).chaseExitSubtree();
                    case Just(var newRight) ->
                            Maybe.of(
                                    new TraceZipper(
                                            treeZipper.replace(
                                                    new Tuple<>(
                                                            newChildIndex, Maybe.of(newRight)))));
                };
            }
        };
    }

    private Maybe<TraceZipper> chaseToNextAtom() {
        Maybe<TraceZipper> result = Maybe.of(this);

        while (true) {
            switch (result) {
                case Nothing():
                    return new Nothing<>();
                case Just(var traceZipper):
                    {
                        var tuple = traceZipper.composedZipper.value();

                        switch (tuple.second()) {
                            // This occurs if we have descended into a generator which
                            // never requested a DrawAtom, resulting in the zipper for this
                            // generator being empty, or if we have already used up all of the
                            // atoms for the current generator (i.e. moved all the way to the
                            // right)
                            case Nothing():
                                {
                                    result = traceZipper.chaseExitSubtree();
                                    continue;
                                }
                            // We are in a generator which still has DrawAtoms remaining
                            case Just(var innerZipper):
                                {
                                    switch (innerZipper.focus()) {
                                        case Down():
                                            {
                                                // TODO not sure about this
                                                // The idea here is that if we descend into a
                                                // generator which never pulled a value, we should
                                                // then just exit back out of it and continue
                                                var down = traceZipper.down();
                                                result =
                                                        switch (down) {
                                                            case Nothing() ->
                                                                    traceZipper.chaseExitSubtree();
                                                            case Just(var ignored) -> down;
                                                        };

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
                    var right = traceZipper.right();
                    result =
                            switch (right) {
                                case Nothing() ->
                                        traceZipper
                                                .chaseExitSubtree()
                                                .chain(TraceZipper::chaseToNextAtom);
                                case Just(var value) -> right.chain(TraceZipper::chaseToNextAtom);
                            };
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
                                tuple ->
                                        tuple.second()
                                                .map(ListZipper::toList)
                                                .orDefault(ImmutableList.empty())));
    }

    public Maybe<DrawAtom> focus() {
        // We want to make sure we're pointing at a Right(), if any Rights remain in the tree.
        Maybe<TraceZipper> normalized = chaseToNextAtom();

        return normalized.chain(
                traceZipper ->
                        traceZipper
                                .composedZipper
                                .value()
                                .second()
                                .map(
                                        innerZipper -> {
                                            TraceEntry focus = innerZipper.focus();

                                            // Because of the invariant enforced by
                                            // chaseToAtomIndex, we know we're pointing at a Right.
                                            return ((Right) focus).atom();
                                        }));
    }

    public Maybe<TraceZipper> replace(DrawAtom atom) {
        // normalized will point to a Right, if it exists
        Maybe<TraceZipper> normalized = chaseToNextAtom();

        // This will always be a Just, because of chaseToNextAtom's invariant
        Function<
                        Tuple<Integer, Maybe<ListZipper<TraceEntry>>>,
                        Tuple<Integer, Maybe<ListZipper<TraceEntry>>>>
                updateInnerZipper =
                        (tuple ->
                                new Tuple<>(
                                        tuple.first(),
                                        tuple.second()
                                                .map(
                                                        innerZipper ->
                                                                innerZipper.replace(
                                                                        new Right(atom)))));

        return normalized.map(
                historyZipper ->
                        new TraceZipper(historyZipper.composedZipper.update(updateInnerZipper)));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TraceZipper that)) return false;
        return Objects.equals(composedZipper, that.composedZipper);
    }

    @Override
    public int hashCode() {
        return Objects.hash(composedZipper);
    }
}
