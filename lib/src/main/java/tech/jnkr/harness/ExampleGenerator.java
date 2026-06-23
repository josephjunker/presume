package tech.jnkr.harness;

import tech.jnkr.presume.Generator;

public class ExampleGenerator extends Generator<Boolean> {
    @Override
    protected Boolean gen(Drawer drawer) {
        return drawer.genBoolean();
    }
}
