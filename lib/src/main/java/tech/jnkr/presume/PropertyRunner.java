package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.*;
import tech.jnkr.presume.internal.History;
import tech.jnkr.presume.internal.shrinking.Failure;
import tech.jnkr.presume.internal.shrinking.Shrinker;
import tech.jnkr.presume.internal.utilities.ImmutableList;
import tech.jnkr.presume.internal.utilities.Maybe;
import tech.jnkr.presume.internal.utilities.MutableRoseTree;

import java.util.ArrayList;
import java.util.function.Consumer;

public class PropertyRunner<T> {

    AbstractGenerator<T> generator;
    Consumer<T> property;

    public static <T> void runProperty(AbstractGenerator<T> generator, Consumer<T> property) {
        runProperty(generator, property, 100);
    }

    public static <T> void runProperty(
            AbstractGenerator<T> generator, Consumer<T> property, int runCount) {
        PropertyRunner<T> runner = new PropertyRunner<>(generator, property);

        for (int i = 0; i < runCount; i++) runner.runOnce();
    }

    public static <T> void runSlug(
            AbstractGenerator<T> generator, Consumer<T> property, String slug) {
        PropertyRunner<T> runner = new PropertyRunner<>(generator, property);

        History history = History.fromSlug(slug);
        ReplayingSource source = new ReplayingSource(history);

        T value;

        try {
            value = source.call(generator);
        } catch (Throwable e) {
            throw new GeneratorThrewException(e.toString());
        }

        runner.validateOnce(value, history);
    }

    public PropertyRunner(AbstractGenerator<T> generator, Consumer<T> property) {
        this.generator = generator;
        this.property = property;
    }

    private void runOnce() {
        RecordingSource recordingSource = new RecordingSource();
        T value;

        try {
            value = recordingSource.call(generator);
        } catch (Throwable e) {
            throw new GeneratorThrewException(e.toString());
        }

        History history = new History(recordingSource.getHistory().map(ImmutableList::toArrayList));
        validateOnce(value, history);
    }

    private void validateOnce(T value, History recordedHistory) {
        try {
            property.accept(value);
        } catch (Throwable e) {

            // The property has failed! Now we need to collect things so we can shrink.
            // We're on the "slow path" now, so we'll rerun with the recordingSource that will
            // gather more detailed tracing information.
            ReplayingSource replayingSource = new ReplayingSource(recordedHistory);
            T replayedValue;

            try {
                // We have to re-run generation to get the detailed trace
                replayedValue = replayingSource.call(generator);
            } catch (Throwable e2) {
                // Running generation twice should give us identical results, so if there's an
                // exception while generating the data the second time it's due to a bad
                // user-provided generator.
                throw new FlakyGeneratorException(value, e2.toString());
            }

            // We need to make absolutely sure that our replayed value gives the same exception that
            // we will report to the user, without assuming nondeterminism. If the user writes a bad
            // generator we still need to show them an exception which corresponds to the
            // counterexample. So let's re-run our re-generated value and re-record the exception it
            // produces.
            try {
                property.accept(replayedValue);
            } catch (Throwable e3) {
                // Now we have a failure, exception, and trace that we know all go together.
                Failure<T> failure = new Failure<>(replayedValue, replayingSource.finalTrace(), e3);

                // Make results small
                Shrinker<T> shrinker = getShrinker(generator, property);
                var shrinkResults = shrinker.shrink(failure);

                // And report them to the user
                throw new CounterexampleException(
                        shrinkResults.timesShrunk(),
                        shrinkResults.failure().trace().toHistory().toSlug(),
                        shrinkResults.failure().counterexample(),

                        // It would be nice if we could pass in the actual exception, instead of
                        // stringifying it, but that wouldn't work. JUnit assertions throw Errors,
                        // not Exceptions, and apparently putting an Error as the cause of an
                        // Exception makes Java (Or maybe just JUnit?) report the Error instead of
                        // the Exception. So we have to shoehorn things here if we want standard
                        // test assertions to not ruin things.
                        shrinkResults.failure().exception().toString());
            }

            // Uh-oh, replaying the value didn't cause an exception the second time! Something is
            // wrong, let the user know.
            throw new NondeterminismException(value, e, replayedValue);
        }
    }

    public static <T> Shrinker<T> getShrinker(
            AbstractGenerator<T> generator, Consumer<T> property) {
        return new Shrinker<T>(
                history -> {
                    ReplayingSource source =
                            new ReplayingSource(history, new MutableRoseTree<>(new ArrayList<>()));
                    T shrunk;

                    try {
                        shrunk = source.call(generator);
                    } catch (SourceDepletedException sourceDepletedException) {
                        // We ran out of atoms and failed to generate input
                        return Maybe.empty();
                    } catch (Throwable e) {
                        throw new GeneratorThrewException(e.toString());
                    }

                    try {
                        property.accept(shrunk);
                    } catch (Throwable exn) {
                        // The property threw; we successfully shrunk
                        return Maybe.of(new Failure<>(shrunk, source.finalTrace(), exn));
                    }
                    // The property passed, meaning that the shrink failed.
                    return Maybe.empty();
                });
    }
}
