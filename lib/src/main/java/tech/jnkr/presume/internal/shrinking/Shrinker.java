package tech.jnkr.presume.internal.shrinking;

import tech.jnkr.presume.internal.HistoryZipper;
import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.utilities.*;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class Shrinker {
    Function<RoseTree<ArrayList<DrawAtom>>, Maybe<RoseTree<ImmutableList<TraceEntry>>>>
            tryReproduce;

    public Shrinker(
            Function<RoseTree<ArrayList<DrawAtom>>, Maybe<RoseTree<ImmutableList<TraceEntry>>>>
                    tryReproduce) {
        this.tryReproduce = tryReproduce;
    }

    public RoseTree<ImmutableList<TraceEntry>> shrink(RoseTree<ImmutableList<TraceEntry>> trace) {
        var lastTrace = trace;
        var currentTrace = doShrinkingPass(trace);
        int i = 0;

        // TODO: implement the relevant equals methods
        // TODO: add a timer here, shrink for up to 5 seconds instead of using i
        while (i < 5 && !lastTrace.equals(currentTrace)) {
            lastTrace = currentTrace;
            currentTrace = doShrinkingPass(currentTrace);
            i++;
        }

        return currentTrace;
    }

    private RoseTree<ImmutableList<TraceEntry>> doShrinkingPass(
            RoseTree<ImmutableList<TraceEntry>> trace) {
        int index = 0;
        var currentTrace = trace;

        while (index < countAtoms(currentTrace)) {
            var optionalShrunk =
                    getTargetAtomShrinks(currentTrace, index)
                            .limit(10)
                            // We don't need exception handling around SourceDepletedException in
                            // here, because we assume that tryReproduce will handle it for us.
                            .map(tryReproduce)
                            .filter(Maybe::isJust)
                            .findFirst();

            if (optionalShrunk.isEmpty()) {
                // We failed to reduce the test case by shrinking the current atom.
                // Move on to attempt the next atom.
                index++;
            } else {
                // `unwrapUnsafe` is safe because of the `filter` above.
                currentTrace = optionalShrunk.get().unwrapUnsafe();
                index++;
            }
        }

        return currentTrace;
    }

    private int countAtoms(RoseTree<ImmutableList<TraceEntry>> trace) {
        // TODO
    }

    private Stream<RoseTree<ArrayList<DrawAtom>>> getTargetAtomShrinks(
            RoseTree<ImmutableList<TraceEntry>> trace, int targetAtomIndex) {
        Maybe<HistoryZipper> maybeZipper =
                HistoryZipper.fromTrace(trace).chaseToAtomIndex(targetAtomIndex);

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
