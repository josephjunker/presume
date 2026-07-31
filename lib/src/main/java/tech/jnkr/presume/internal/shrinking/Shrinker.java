package tech.jnkr.presume.internal.shrinking;

import tech.jnkr.presume.internal.HistoryZipper;
import tech.jnkr.presume.internal.atoms.*;
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

    public RoseTree<ArrayList<DrawAtom>> shrink(RoseTree<ImmutableList<TraceEntry>> trace) {
        var lastTrace = trace;
        var currentTrace = doShrinkingPass(trace);
        int i = 0;

        // TODO: add a timer here, shrink for up to 10 seconds instead of using i
        while (i < 5 && !lastTrace.equals(currentTrace)) {
            lastTrace = currentTrace;
            currentTrace = doShrinkingPass(currentTrace);
            i++;
        }

        // TODO: weird to go through a zipper for this, this should be a standalone helper
        return HistoryZipper.fromTrace(currentTrace).toHistory();
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
                            // here, because tryReproduce will handle it for us.
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
        return trace.foldDepthFirst(
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
                                    Stream<DrawAtom> shrinks = shrinkAtom(atom);

                                    yield shrinks.flatMap(
                                            smallerAtom -> replaceFocus.apply(zipper, smallerAtom));
                                }
                            };
                };

        return shrunkZippers.map(HistoryZipper::toHistory);
    }

    private Stream<DrawAtom> shrinkAtom(DrawAtom atom) {
        return switch (atom) {
            case Trivial1() -> Stream.of();
            case Trivial2() -> Stream.of(new Trivial1());
            case Regular(float ratio, boolean sign, boolean simplify) -> {
                Stream<DrawAtom> trivials = Stream.of(new Trivial1(), new Trivial2());
                Stream<DrawAtom> flags = simplifyFlags(ratio, sign, simplify);
                Stream<Regular> reduced =
                        Stream.iterate(
                                new Regular(ratio / 2f, sign, simplify),
                                r -> new Regular(r.ratio() / 2f, r.sign(), r.simplify()));

                yield Stream.concat(
                        Stream.concat(trivials, flags),
                        reduced.flatMap(
                                r ->
                                        Stream.concat(
                                                Stream.of(r),
                                                simplifyFlags(r.ratio(), r.sign(), r.simplify()))));
            }
            case Edge(float ratio, boolean sign) ->
                    Stream.of(new Trivial1(), new Trivial2(), new Regular(ratio, sign, false));
        };
    }

    private Stream<DrawAtom> simplifyFlags(float ratio, boolean sign, boolean simplify) {
        if (sign && simplify) return Stream.of();
        if (!sign && !simplify)
            return Stream.of(
                    new Regular(ratio, true, true),
                    new Regular(ratio, true, false),
                    new Regular(ratio, false, true));
        if (!sign)
            return Stream.of(new Regular(ratio, true, true), new Regular(ratio, true, false));

        return Stream.of(new Regular(ratio, true, true), new Regular(ratio, false, true));
    }
}
