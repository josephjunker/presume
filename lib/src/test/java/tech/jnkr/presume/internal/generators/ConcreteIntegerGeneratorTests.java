package tech.jnkr.presume.internal.generators;

import org.junit.jupiter.api.Test;

import tech.jnkr.presume.internal.atoms.AtomSource;
import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.atoms.Regular;

import java.util.Random;

public class ConcreteIntegerGeneratorTests {
    @Test
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

        System.out.printf("Even: %d, Odd: %d\n", evenCount, oddCount);
    }

    @Test
    public void wtf() {
        Random random = new Random();
        int evenCount = 0;
        int oddCount = 0;
        for (int i = 0; i < 1000; i++) {
            if (((int) ((float) Integer.MAX_VALUE * random.nextFloat())) % 2 == 0) {
                evenCount++;
            } else {
                oddCount++;
            }
        }

        System.out.printf("Even: %d, Odd: %d\n", evenCount, oddCount);
    }

    public static DrawAtom getRegular() {
        AtomSource source = new AtomSource();
        DrawAtom atom = source.getAtom();

        return atom instanceof Regular ? atom : getRegular();
    }
}
