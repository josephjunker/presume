package tech.jnkr.presume;

import java.util.function.Consumer;

public class PropertyRunner {
    public <T> void run(Generator<T> generator, Consumer<T> property) {
        Generator.Drawer baseDrawer = new Generator.Drawer();
        T value = baseDrawer.call(generator);
        property.accept(value);
    }
}
