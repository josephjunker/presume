package tech.jnkr.presume.integration;

import org.jspecify.annotations.NonNull;

import tech.jnkr.presume.AbstractGenerator;
import tech.jnkr.presume.GenerationSource;

public class SimpleBooleanGenerator extends AbstractGenerator<Boolean> {
    @Override
    protected Boolean gen(@NonNull GenerationSource drawer) {
        return drawer.getBoolean();
    }
}
