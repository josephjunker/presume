package tech.jnkr.presume;

import org.junit.jupiter.api.Test;

import tech.jnkr.presume.internal.atoms.AtomSource;
import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.atoms.Regular;

public class BooleanGeneratorTests {

    @Test
    public void shouldProduceEvenDistribution() {
        BooleanGenerator gen = new BooleanGenerator(BooleanGeneratorTests::getRegular);
        StatsCollector<Boolean> collector = new StatsCollector<>(gen);
        collector
                .addBooleanBucket("Value", x -> x)
                .withMinimumTrueRatio(0.4f)
                .withMinimumFalseRatio(0.4f);

        collector.summarize(10_000);
    }

    public static DrawAtom getRegular() {
        AtomSource source = new AtomSource();
        DrawAtom atom = source.getAtom();

        return atom instanceof Regular ? atom : getRegular();
    }
}
