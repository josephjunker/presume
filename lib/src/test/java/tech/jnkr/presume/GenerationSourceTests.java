package tech.jnkr.presume;

import org.junit.jupiter.api.Test;

import tech.jnkr.presume.generators.GenerationSource;

public class GenerationSourceTests {

    @Test
    public void weightedBoolean_shouldMostlyWork() {
        float pointFive = runWeightedBoolean(0.5f);
        System.out.println(pointFive);
    }

    private float runWeightedBoolean(float ratio) {
        int trueCount = 0;
        GenerationSource source = new PureSource();

        for (int i = 0; i < 10000; i++) {
            boolean flip = source.getWeightedBoolean(ratio);
            if (flip) {
                trueCount++;
            }
        }

        return 1.0f / (float) trueCount;
    }
}
