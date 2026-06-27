package tech.jnkr.presume;

import java.util.function.Consumer;

public class PropertyRunner {
    public static <T> void runProperty(Generator<T> generator, Consumer<T> property) {
        RecordingSource recordingSource = new RecordingSource();
        T value = recordingSource.call(generator);
        property.accept(value);
    }
}
