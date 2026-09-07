package tech.jnkr.presume.internal.generators;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.RepeatedTest;

import tech.jnkr.presume.internal.atoms.AtomSource;
import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.atoms.Regular;

public class ConcreteIntegerGeneratorTests {
    @RepeatedTest(10)
    public void shouldProduceBothOddAndEvenNumbers() {
        ConcreteIntegerGenerator generator =
                new ConcreteIntegerGenerator(ConcreteIntegerGeneratorTests::getRegular);

        int evenCount = 0;
        int oddCount = 0;
        for (int i = 0; i < 1000; i++) {
            if (generator.gen() % 2 == 0) {
                evenCount++;
            } else {
                oddCount++;
            }
        }

        assertTrue(evenCount > 250);
        assertTrue(oddCount > 250);
    }

    public void shouldProduceRelativelyEvenDistribution() {}

    public static DrawAtom getRegular() {
        AtomSource source = new AtomSource();
        DrawAtom atom = source.getAtom();

        return atom instanceof Regular ? atom : getRegular();
    }
}
