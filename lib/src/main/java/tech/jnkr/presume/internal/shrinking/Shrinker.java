package tech.jnkr.presume.internal.shrinking;

import tech.jnkr.presume.internal.History;
import tech.jnkr.presume.internal.Trace;
import tech.jnkr.presume.internal.TraceZipper;
import tech.jnkr.presume.internal.atoms.*;
import tech.jnkr.presume.internal.utilities.*;

import java.util.ArrayList;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class Shrinker<T> {
    Function<History, Maybe<Failure<T>>> tryReproduce;

    public Shrinker(Function<History, Maybe<Failure<T>>> tryReproduce) {
        this.tryReproduce = tryReproduce;
    }

    public ShrinkResults<T> shrink(Failure<T> failure) {
        ShrinkResults<T> lastShrinkResults = new ShrinkResults<>(failure, 0);
        ShrinkResults<T> currentShrinkResults = doAtomShrinkingPass(lastShrinkResults.failure);
        int i = 0;

        // TODO: add a timer here, shrink for up to 10 additional seconds instead of using i
        while (i < 10000
                && !lastShrinkResults
                        .failure
                        .trace()
                        .equals(currentShrinkResults.failure.trace())) {
            lastShrinkResults =
                    new ShrinkResults<>(
                            currentShrinkResults.failure,
                            currentShrinkResults.timesShrunk + lastShrinkResults.timesShrunk);
            currentShrinkResults = doAtomShrinkingPass(currentShrinkResults.failure);

            Maybe<ShrinkResults<T>> treeShrinkResults =
                    doTreeShrinkingPass(currentShrinkResults.failure.trace().toHistory());

            switch (treeShrinkResults) {
                case Nothing():
                    break;
                case Just(var treeShrunk):
                    {
                        currentShrinkResults =
                                new ShrinkResults<>(
                                        treeShrunk.failure,
                                        currentShrinkResults.timesShrunk + treeShrunk.timesShrunk);
                    }
            }
            i++;
        }

        System.out.println("!!!!!!");
        System.out.println(i);

        return new ShrinkResults<>(
                currentShrinkResults.failure,
                lastShrinkResults.timesShrunk + currentShrinkResults.timesShrunk);
    }

    private Maybe<ShrinkResults<T>> doTreeShrinkingPass(History history) {
        Stack<ImmutableList<Integer>> paths = new Stack<>();
        paths.add(ImmutableList.empty());

        History currentHistory = history;
        Maybe<Failure<T>> lastFailure = Maybe.empty();

        int shrinkCount = 0;

        while (!paths.isEmpty()) {
            var path = paths.pop();
            var maybeZipper = currentHistory.contents.toZipper().followPath(path);

            switch (maybeZipper) {
                case Nothing():
                    continue;
                case Just(var zipper):
                    {
                        var subtrees = zipper.getCurrentSubtree().getSubtrees(3);
                        var candidates = subtrees.map(zipper::replaceCurrentSubtree);

                        Maybe<Failure<T>> maybeFailure = findSubtreeFailure(candidates);

                        switch (maybeFailure) {
                            case Nothing():
                                {
                                    for (int i = 0; i < zipper.childCount(); i++) {
                                        paths.add(path.push(i));
                                    }
                                    continue;
                                }
                            case Just(var failure):
                                {
                                    shrinkCount++;

                                    lastFailure = Maybe.of(failure);
                                    currentHistory = failure.trace().toHistory();

                                    for (int i = 0;
                                            i < currentHistory.contents.children.size();
                                            i++) {
                                        paths.add(path.push(i));
                                    }
                                }
                        }
                    }
            }
        }

        return switch (lastFailure) {
            case Nothing() -> Maybe.empty();
            case Just(var failure) -> Maybe.of(new ShrinkResults<>(failure, shrinkCount));
        };
    }

    private Maybe<Failure<T>> findSubtreeFailure(
            ImmutableList<RoseTreeZipper<ArrayList<DrawAtom>>> candidates) {
        for (var candidate : candidates) {
            Maybe<Failure<T>> maybeFailure = tryReproduce.apply(new History(candidate.toTree()));

            switch (maybeFailure) {
                case Nothing():
                    continue;
                case Just(Failure<T> failure):
                    return maybeFailure;
            }
        }

        return Maybe.empty();
    }

    private ShrinkResults<T> doAtomShrinkingPass(Failure<T> failure) {
        int index = 0;
        var currentFailure = failure;
        int timesShrunk = 0;

        while (index < currentFailure.trace().atomCount()) {
            var optionalShrunk =
                    getTargetAtomShrinks(currentFailure.trace(), index)
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
                currentFailure = optionalShrunk.get().unwrapUnsafe();
                index++;
                timesShrunk++;
            }
        }

        return new ShrinkResults<>(currentFailure, timesShrunk);
    }

    public record ShrinkResults<T>(Failure<T> failure, Integer timesShrunk) {}

    private Stream<History> getTargetAtomShrinks(Trace trace, int targetAtomIndex) {
        Maybe<TraceZipper> maybeZipper =
                TraceZipper.fromTrace(trace).chaseToAtomIndex(targetAtomIndex);

        BiFunction<TraceZipper, DrawAtom, Stream<TraceZipper>> replaceFocus =
                (zipper, atom) ->
                        switch (zipper.replace(atom)) {
                            case Nothing() -> Stream.empty();
                            case Just(TraceZipper newZipper) -> Stream.of(newZipper);
                        };

        Stream<TraceZipper> shrunkZippers =
                switch (maybeZipper) {
                    case Nothing() -> Stream.empty();
                    case Just(TraceZipper zipper) ->
                            switch (zipper.focus()) {
                                case Nothing() -> Stream.empty();
                                case Just(DrawAtom atom) -> {
                                    Stream<DrawAtom> shrinks = shrinkAtom(atom);

                                    yield shrinks.flatMap(
                                            smallerAtom -> replaceFocus.apply(zipper, smallerAtom));
                                }
                            };
                };

        return shrunkZippers.map(zipper -> zipper.toTrace().toHistory());
    }

    private Stream<DrawAtom> shrinkAtom(DrawAtom atom) {
        return switch (atom) {
            case Trivial1() -> Stream.of();
            case Trivial2() -> Stream.of(new Trivial1());
            case Regular(int magnitude, boolean sign, boolean simplify) -> {
                Stream<DrawAtom> trivials = Stream.of(new Trivial1(), new Trivial2());
                Stream<DrawAtom> flags = simplifyFlags(magnitude, sign, simplify);
                Stream<Float> reductionSchedule =
                        Stream.of(
                                1000f, 500f, 100f, 20f, 10f, 2f, 1.5f, 1.4f, 1.25f, 1.1f, 1.05f,
                                1.01f, 1.001f, 1.0001f);

                Stream<DrawAtom> reduced =
                        reductionSchedule.flatMap(
                                reductionRatio ->
                                        Stream.concat(
                                                simplifyFlags(
                                                        (int) ((float) magnitude / reductionRatio),
                                                        sign,
                                                        simplify),
                                                Stream.of(
                                                        new Regular(
                                                                (int)
                                                                        ((float) magnitude
                                                                                / reductionRatio),
                                                                sign,
                                                                simplify))));

                yield Stream.concat(Stream.concat(trivials, flags), reduced);
            }
            case Edge(int magnitude, boolean sign) -> {
                // Any `Regular` is "smaller" than an `Edge` from the shrinker's
                // perspective, so increasing the ratio here doesn't break our guarantees
                Stream<Integer> regularShrinkMagnitudes =
                        Stream.iterate(
                                1,
                                // The choice of these values is arbitrary and not
                                // thoroughly-considered
                                value -> value < 100000,
                                value -> (int) ((float) value * 1.5) + 3);

                Stream<DrawAtom> regularShrinks =
                        regularShrinkMagnitudes.flatMap(
                                syntheticRatio ->
                                        Stream.concat(
                                                simplifyFlags(syntheticRatio, false, false),
                                                Stream.of(
                                                        new Regular(
                                                                syntheticRatio, false, false))));

                yield Stream.concat(
                        Stream.concat(Stream.of(new Trivial1(), new Trivial2()), regularShrinks),
                        simplifyFlags(magnitude, false, false));
            }
        };
    }

    private Stream<DrawAtom> simplifyFlags(int magnitude, boolean sign, boolean simplify) {
        if (sign && simplify) return Stream.of();
        if (!sign && !simplify)
            return Stream.of(
                    new Regular(magnitude, true, true),
                    new Regular(magnitude, true, false),
                    new Regular(magnitude, false, true));
        if (!sign)
            return Stream.of(
                    new Regular(magnitude, true, true), new Regular(magnitude, true, false));

        return Stream.of(new Regular(magnitude, true, true), new Regular(magnitude, false, true));
    }
}
