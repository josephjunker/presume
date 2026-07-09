package tech.jnkr.presume.internal.shrinking;

import tech.jnkr.presume.internal.HistoryZipper;
import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.utilities.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class Shrinker {
    RoseTree<ImmutableList<TraceEntry>> trace;
    Function<RoseTree<List<DrawAtom>>, Maybe<RoseTree<ImmutableList<TraceEntry>>>> tryReproduce;
    int targetAtomIndex;

    public Shrinker(
            RoseTree<ImmutableList<TraceEntry>> trace,
            Function<RoseTree<List<DrawAtom>>, Maybe<RoseTree<ImmutableList<TraceEntry>>>>
                    tryReproduce) {
        this.trace = trace;
        this.tryReproduce = tryReproduce;
        this.targetAtomIndex = 0;
    }

    private Maybe<HistoryZipper> focusAtom() {
        return HistoryZipper.fromTrace(trace).chaseToAtomIndex(targetAtomIndex);
    }

    public Stream<RoseTree<ArrayList<DrawAtom>>> getCurrentAtomShrinks() {
        Maybe<HistoryZipper> maybeZipper = focusAtom();

        BiFunction<HistoryZipper, DrawAtom, Stream<HistoryZipper>> replaceFocus =
                (zipper, atom) ->
                        switch (zipper.replace(atom)) {
                            case Nothing() -> Stream.empty();
                            case Just(HistoryZipper newZipper) -> Stream.of(newZipper);
                        };

        Stream<HistoryZipper> shrunkZippers =
                switch (maybeZipper) {
                    case Nothing() -> Stream.empty();
                    case Just(HistoryZipper zipper) ->
                            switch (zipper.focus()) {
                                case Nothing() -> Stream.empty();
                                case Just(DrawAtom atom) -> {
                                    Stream<DrawAtom> shrinks = atom.shrink();

                                    yield shrinks.flatMap(
                                            smallerAtom -> replaceFocus.apply(zipper, smallerAtom));
                                }
                            };
                };

        return shrunkZippers.map(HistoryZipper::toHistory);
    }
}
