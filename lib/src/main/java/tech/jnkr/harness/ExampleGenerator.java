package tech.jnkr.harness;

import tech.jnkr.presume.AbstractGenerator;
import tech.jnkr.presume.generators.GenerationSource;

public class ExampleGenerator extends AbstractGenerator<Boolean> {
    @Override
    protected Boolean gen(GenerationSource drawer) {
        return drawer.getBoolean();
    }
}
