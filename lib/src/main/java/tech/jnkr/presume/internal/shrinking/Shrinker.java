package tech.jnkr.presume.internal.shrinking;

import tech.jnkr.presume.internal.History;
import tech.jnkr.presume.internal.Trace;
import tech.jnkr.presume.internal.TraceZipper;
import tech.jnkr.presume.internal.atoms.*;
import tech.jnkr.presume.internal.utilities.*;

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
        ShrinkResults<T> currentShrinkResults = doShrinkingPass(lastShrinkResults.failure);
        int i = 0;

        // TODO: add a timer here, shrink for up to 10 additional seconds instead of using i
        while (i < 5
                && !lastShrinkResults
                        .failure
                        .trace()
                        .equals(currentShrinkResults.failure.trace())) {
            lastShrinkResults =
                    new ShrinkResults<>(
                            currentShrinkResults.failure,
                            currentShrinkResults.timesShrunk + lastShrinkResults.timesShrunk);
            currentShrinkResults = doShrinkingPass(currentShrinkResults.failure);
            i++;
        }

        return new ShrinkResults<>(
                currentShrinkResults.failure,
                lastShrinkResults.timesShrunk + currentShrinkResults.timesShrunk);
    }

    private ShrinkResults<T> doShrinkingPass(Failure<T> failure) {
        int index = 0;
        var currentFailure = failure;
        int timesShrunk = 0;

        while (index < currentFailure.trace().atomCount()) {
            var optionalShrunk =
                    getTargetAtomShrinks(currentFailure.trace(), index)
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
