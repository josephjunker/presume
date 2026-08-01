package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.CounterexampleException;
import tech.jnkr.presume.exceptions.NondeterministicGeneratorException;
import tech.jnkr.presume.exceptions.SourceDepletedException;
import tech.jnkr.presume.internal.History;
import tech.jnkr.presume.internal.shrinking.Shrinker;
import tech.jnkr.presume.internal.utilities.ImmutableList;
import tech.jnkr.presume.internal.utilities.Maybe;
import tech.jnkr.presume.internal.utilities.MutableRoseTree;

import java.util.ArrayList;
import java.util.function.Consumer;

public class PropertyRunner {
    public static <T> void runProperty(Generator<T> generator, Consumer<T> property) {
        RecordingSource recordingSource = new RecordingSource();
        T value = recordingSource.call(generator);
        try {
            property.accept(value);
        } catch (Exception e) {
            ReplayingSource replayingSource =
                    new ReplayingSource(
                            new History(
                                    recordingSource.getHistory().map(ImmutableList::toArrayList)),
                            new MutableRoseTree<>(new ArrayList<>()));

            try {
                // We have to re-run generation to get the detailed trace
                replayingSource.call(generator);
            } catch (Exception e2) {
                // Running generation twice should give us identical results, so if there's an
                // exception it's due to a bad user-provided generator.
                throw new NondeterministicGeneratorException(e2);
            }

            Shrinker shrinker =
                    new Shrinker(
                            history -> {
                                ReplayingSource source =
                                        new ReplayingSource(
                                                history, new MutableRoseTree<>(new ArrayList<>()));
                                try {
                                    T shrunk = source.call(generator);
                                    property.accept(shrunk);
                                } catch (SourceDepletedException sourceDepletedException) {
                                    // We failed to generate input
                                    return Maybe.empty();
                                } catch (Exception otherException) {
                                    // The property threw; we successfully shrunk
                                    return Maybe.of(source.finalTrace());
                                }
                                // The property passed, meaning that the shrink failed.
                                return Maybe.empty();
                            });

            var counterexampleHistory = shrinker.shrink(replayingSource.finalTrace());

            try {
                T counterexample =
                        new ReplayingSource(counterexampleHistory.first()).call(generator);
                // TODO collect stringified seed
                throw new CounterexampleException(
                        counterexampleHistory.second(), "", counterexample);
            } catch (Exception e3) {
                throw new NondeterministicGeneratorException(e3);
            }
        }
    }
}
