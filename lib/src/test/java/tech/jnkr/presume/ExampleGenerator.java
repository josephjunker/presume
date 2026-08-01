package tech.jnkr.presume;

import tech.jnkr.presume.generators.GenerationSource;

public class ExampleGenerator extends AbstractGenerator<Boolean> {
    @Override
    protected Boolean gen(GenerationSource drawer) {
        return drawer.getBoolean();
    }
}
