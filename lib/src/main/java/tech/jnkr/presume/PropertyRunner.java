package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.SourceDepletedException;
import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.utilities.MutableRoseTree;
import tech.jnkr.presume.internal.utilities.RoseTree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PropertyRunner {
    public static <T> void runProperty(Generator<T> generator, Consumer<T> property) {
        RecordingSource recordingSource = new RecordingSource();
        T value = recordingSource.call(generator);
        try {
            property.accept(value);
        } catch (Exception e) {
            System.out.println("oh no");
            // Counterexample counterexample = new Counterexample(recordingSource.getHistory());
            /*
            Counterexample minimal =
                    counterexample.shrink(
                            (history) -> {
                                try {
                                    ReplayingSource replayingSource = new ReplayingSource(history);
                                    T attempt = replayingSource.call(generator);
                                    property.accept(attempt);
                                } catch (SourceDepletedException e2) {
                                    return false;
                                } catch (Exception e3) {
                                    return true;
                                }

                                return false;
                            });
                            */
        }
    }

    private static <T> void shrink(
            Generator<T> generator, Consumer<T> property, RoseTree<List<DrawAtom>> counterexample) {
        ReplayingSource replayingSource =
                new ReplayingSource(counterexample, new MutableRoseTree<>(new ArrayList<>()));
        T value = null;
        try {
            value = replayingSource.call(generator);
        } catch (SourceDepletedException e) {

        }

        property.accept(value);
    }
}
