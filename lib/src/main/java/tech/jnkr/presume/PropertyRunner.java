package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.NondeterministicGeneratorException;
import tech.jnkr.presume.internal.utilities.ImmutableList;
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
                            recordingSource.getHistory().map(ImmutableList::toArrayList),
                            new MutableRoseTree<>(new ArrayList<>()));

            try {
                // We have to re-run generation to get the detailed trace
                replayingSource.call(generator);
            } catch (Exception e2) {
                // Running generation twice should give us identical results, so if there's an
                // exception it's due to a bad user-provided generator.
                throw new NondeterministicGeneratorException(e2);
            }

            var trace = replayingSource.finalTrace();
            // TODO: wire into Shrinker here
        }
    }
}
