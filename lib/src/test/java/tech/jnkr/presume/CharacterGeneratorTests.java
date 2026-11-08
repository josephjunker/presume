package tech.jnkr.presume;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import tech.jnkr.presume.internal.atoms.AtomSource;
import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.atoms.Regular;

public class CharacterGeneratorTests {

    @ParameterizedTest
    @CsvSource({
        "0, 65535",
        "100, 65535",
        "127, 65535",
        "128, 65535",
        "129, 65535",
        "1024, 65535",
        "20000, 65535",
        "60000, 65535",
        "65530, 65535",
        "65533, 65535",
        "65534, 65535",
        "65535, 65535",
        "0, 0",
        "0, 1",
        "0, 2",
        "0, 5",
        "0, 100",
        "0, 127",
        "0, 128",
        "0, 129",
        "0, 20000",
        "0, 50001",
        "120, 130",
        "127, 128",
        "127, 129",
        "128, 128",
        "128, 129",
        "128, 130",
        "129, 129",
        "30000, 30100",
        "30000, 30127",
        "30000, 30128",
        "30000, 30129"
    })
    public void shouldProduceValuesInRange(int min, int max) {
        CharacterGenerator gen =
                new CharacterGenerator(CharacterGeneratorTests::getRegular)
                        .withRange((char) min, (char) max);

        for (int i = 0; i < 1000; i++) {
            assertThat((int) gen.genPrimitive())
                    .isGreaterThanOrEqualTo(min)
                    .isLessThanOrEqualTo(max);
        }
    }

    public static DrawAtom getRegular() {
        AtomSource source = new AtomSource();
        DrawAtom atom = source.getAtom();

        return atom instanceof Regular ? atom : getRegular();
    }
}
