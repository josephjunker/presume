package tech.jnkr.harness;

import tech.jnkr.presume.GenerationSource;
import tech.jnkr.presume.Generator;

public class ExampleGenerator extends Generator<Boolean> {
    @Override
    protected Boolean gen(GenerationSource drawer) {
        return drawer.genBoolean();
    }
}
