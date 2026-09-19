package tech.jnkr.presume.internal.shrinking;

import tech.jnkr.presume.internal.History;
import tech.jnkr.presume.internal.Trace;
import tech.jnkr.presume.internal.TraceZipper;
import tech.jnkr.presume.internal.atoms.*;
import tech.jnkr.presume.internal.utilities.*;

import java.time.Instant;
import java.util.ArrayList;
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
        ShrinkResults<T> currentShrinkResults = doShrinkingPass(lastShrinkResults.failure, false);
        int i = 0;

        long startSeconds = Instant.now().getEpochSecond();
        long currentSeconds = startSeconds;

        while (currentSeconds - startSeconds < 10
                && !lastShrinkResults
                        .failure
                        .trace()
                        .equals(currentShrinkResults.failure.trace())) {
            lastShrinkResults =
                    new ShrinkResults<>(
                            currentShrinkResults.failure,
                            currentShrinkResults.timesShrunk + lastShrinkResults.timesShrunk);
            currentShrinkResults = doShrinkingPass(lastShrinkResults.failure, false);
            i++;
            currentSeconds = Instant.now().getEpochSecond();
        }

        // Same as above, but we do fine shrinking steps.
        if (currentSeconds - startSeconds < 10) {
            do {
                lastShrinkResults =
                        new ShrinkResults<>(
                                currentShrinkResults.failure,
                                currentShrinkResults.timesShrunk + lastShrinkResults.timesShrunk);
                currentShrinkResults = doShrinkingPass(lastShrinkResults.failure, true);
                i++;
                currentSeconds = Instant.now().getEpochSecond();
            } while (currentSeconds - startSeconds < 10
                    && !lastShrinkResults
                            .failure
                            .trace()
                            .equals(currentShrinkResults.failure.trace()));
        }

        return new ShrinkResults<>(
                currentShrinkResults.failure,
                lastShrinkResults.timesShrunk + currentShrinkResults.timesShrunk);
    }

    private ShrinkResults<T> doShrinkingPass(Failure<T> failure, boolean useFineReductions) {
        ShrinkResults<T> atomShrinks = doAtomShrinkingPass(failure, useFineReductions);
        Maybe<ShrinkResults<T>> treeShrinkResults =
                doTreeShrinkingPass(atomShrinks.failure.trace().toHistory());

        return treeShrinkResults
                .map(results -> new ShrinkResults<>(results.failure, atomShrinks.timesShrunk + 1))
                .orDefault(atomShrinks);
    }

    private Maybe<ShrinkResults<T>> doTreeShrinkingPass(History history) {
        var contexts = history.contents.getContexts();

        for (var zipper : contexts) {
            var subtrees = zipper.getCurrentSubtree().getSubtrees(10);
            var candidates = subtrees.map(zipper::replaceCurrentSubtree);

            Maybe<Failure<T>> maybeFailure = findSubtreeFailure(candidates);

            switch (maybeFailure) {
                case Nothing():
                    continue;
                case Just(var failure):
                    return Maybe.of(new ShrinkResults<>(failure, 1));
            }
        }

        return Maybe.empty();
    }

    private Maybe<Failure<T>> findSubtreeFailure(
            ImmutableList<RoseTreeZipper<ArrayList<DrawAtom>>> candidates) {

        for (var candidate : candidates) {
            Maybe<Failure<T>> maybeFailure = tryReproduce.apply(new History(candidate.toTree()));
            if (maybeFailure.isJust()) return maybeFailure;
        }

        return Maybe.empty();
    }

    private ShrinkResults<T> doAtomShrinkingPass(Failure<T> failure, boolean useFineReductions) {
        int index = 0;
        var currentFailure = failure;
        int timesShrunk = 0;

        while (index < currentFailure.trace().atomCount()) {
            var optionalShrunk =
                    getTargetAtomShrinks(currentFailure.trace(), index, useFineReductions)
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

    private Stream<History> getTargetAtomShrinks(
            Trace trace, int targetAtomIndex, boolean useFineReductions) {
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
                                    Stream<DrawAtom> shrinks = shrinkAtom(atom, useFineReductions);

                                    yield shrinks.flatMap(
                                            smallerAtom -> replaceFocus.apply(zipper, smallerAtom));
                                }
                            };
                };

        return shrunkZippers.map(zipper -> zipper.toTrace().toHistory());
    }

    private Stream<DrawAtom> shrinkAtom(DrawAtom atom, boolean useFineReductions) {
        return switch (atom) {
            case Trivial1 ignored -> Stream.of();
            case Trivial2 ignored -> Stream.of(new Trivial1());
            case Regular(int magnitude, boolean sign, boolean simplify) -> {
                Stream<DrawAtom> trivials = Stream.of(new Trivial1(), new Trivial2());
                Stream<DrawAtom> flags = simplifyFlags(magnitude, sign, simplify);
                Stream<Float> coarseReductionSchedule =
                        Stream.of(
                                1000f, 500f, 100f, 20f, 10f, 2f, 1.5f, 1.4f, 1.25f, 1.1f, 1.05f,
                                1.01f, 1.001f, 1.0001f);

                Stream<Integer> coarseReducedMagnitudes =
                        coarseReductionSchedule.map(
                                reductionRatio -> (int) ((float) magnitude / reductionRatio));

                Stream<DrawAtom> coarseReduced =
                        shrunkAtomsFromMagnitudes(coarseReducedMagnitudes, sign, simplify);

                Stream<Integer> fineReducedMagnitudes =
                        useFineReductions
                                ? Stream.concat(
                                        Stream.iterate(0, i -> i <= 100, i -> i + 1),
                                        Stream.iterate(
                                                magnitude, i -> magnitude - i <= 100, i -> i - 1))
                                : Stream.of();

                Stream<DrawAtom> fineReduced =
                        shrunkAtomsFromMagnitudes(fineReducedMagnitudes, sign, simplify);

                yield Stream.concat(
                        Stream.concat(Stream.concat(trivials, flags), coarseReduced), fineReduced);
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

    private Stream<DrawAtom> shrunkAtomsFromMagnitudes(
            Stream<Integer> magnitudes, boolean sign, boolean simplify) {
        return magnitudes.flatMap(
                magnitude ->
                        Stream.concat(
                                simplifyFlags(magnitude, sign, simplify),
                                Stream.of(new Regular(magnitude, sign, simplify))));
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
