package tech.jnkr.presume;

import static tech.jnkr.presume.PropertyRunner.runProperty;

import org.junit.jupiter.api.Test;

class TestHarness {
    @Test
    void assertThatAllBooleansAreTrue() {

        runProperty(
                new ExampleGenerator(),
                (bool) -> {
                    // Assert that all booleans are true
                    if (!bool) throw new RuntimeException("Was false");
                });
    }
}
