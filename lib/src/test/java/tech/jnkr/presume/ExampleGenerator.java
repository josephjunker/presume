package tech.jnkr.presume;

import org.jspecify.annotations.NonNull;
import tech.jnkr.presume.generators.GenerationSource;

public class ExampleGenerator extends AbstractGenerator<Boolean> {
    @Override
    protected Boolean gen(@NonNull GenerationSource drawer) {
        return drawer.getBoolean();
    }
}
