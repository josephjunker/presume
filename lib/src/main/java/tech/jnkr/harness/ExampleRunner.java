package tech.jnkr.harness;

import static tech.jnkr.presume.PropertyRunner.runProperty;

public class ExampleRunner {
    void doRun() {
        runProperty(
                new ExampleGenerator(),
                (bool) -> {
                    if (bool != true)
                        throw new RuntimeException("Expected all booleans to be true");
                });
    }
}
