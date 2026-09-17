package tech.jnkr.presume;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class GenerationSourceTests {

    @Test
    public void weightedBoolean_shouldMostlyWork() {
        float pointOne = runWeightedBoolean(0.1f);
        assertTrue(pointOne < 0.2);
        assertTrue(pointOne > 0.05);

        float pointThree = runWeightedBoolean(0.3f);
        assertTrue(pointThree < 0.4);
        assertTrue(pointThree > 0.2);

        float pointFive = runWeightedBoolean(0.5f);
        assertTrue(pointFive < 0.6);
        assertTrue(pointFive > 0.4);

        float pointSix = runWeightedBoolean(0.6f);
        assertTrue(pointSix < 0.7);
        assertTrue(pointSix > 0.5);

        float pointNine = runWeightedBoolean(0.9f);
        assertTrue(pointNine < 0.95);
        assertTrue(pointNine > 0.8);
    }

    private float runWeightedBoolean(float ratio) {
        int trueCount = 0;
        GenerationSource source = new PureSource();

        for (int i = 0; i < 10_000; i++) {
            boolean flip = source.getWeightedBoolean(ratio);
            if (flip) {
                trueCount++;
            }
        }

        return (float) trueCount / 10_000f;
    }
}
