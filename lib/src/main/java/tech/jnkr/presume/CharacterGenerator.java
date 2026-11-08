package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.InvalidGeneratorException;
import tech.jnkr.presume.internal.atoms.*;

import java.util.function.Supplier;

public class CharacterGenerator extends AbstractGenerator<Character>
        implements PrimitiveGenerator<Character> {
    private final char minInclusive;
    private final char maxInclusive;
    private final Supplier<DrawAtom> atomSupplier;

    CharacterGenerator(Supplier<DrawAtom> atomSupplier) {
        this.atomSupplier = atomSupplier;
        minInclusive = Character.MIN_VALUE;
        maxInclusive = Character.MAX_VALUE;
    }

    private CharacterGenerator(
            Supplier<DrawAtom> atomSupplier, Character minInclusive, Character maxInclusive) {
        this.atomSupplier = atomSupplier;
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
    }

    public CharacterGenerator withRange(Character minInclusive, Character maxInclusive) {
        return new CharacterGenerator(atomSupplier, minInclusive, maxInclusive);
    }

    public CharacterGenerator withMinimum(Character minInclusive) {
        return new CharacterGenerator(atomSupplier, minInclusive, this.maxInclusive);
    }

    public CharacterGenerator withMaximum(Character maxInclusive) {
        return new CharacterGenerator(atomSupplier, this.minInclusive, maxInclusive);
    }

    @Override
    protected Character gen(GenerationSource source) {
        return produce(atomSupplier.get());
    }

    @Override
    public Character genPrimitive() {
        return produce(atomSupplier.get());
    }

    private Character produce(DrawAtom atom) {
        if (minInclusive > maxInclusive) {
            throw new InvalidGeneratorException(
                    "Tried to produce a Character whose minimum value was larger than its maximum"
                            + " value");
        }

        return switch (atom) {
            case Trivial1 trivial1 -> minInclusive;
            case Trivial2 trivial2 -> minInclusive;
            case Regular(int magnitude, boolean sign1, boolean simplify) -> {
                float ratio = (float) magnitude / Integer.MAX_VALUE;
                int range = maxInclusive - minInclusive;
                int index = (int) (range * ratio);
                yield (char) (minInclusive + index);
            }
            // We shrink towards positive, so positive corresponds to minimum
            case Edge(int magnitude, boolean sign) -> sign ? minInclusive : maxInclusive;
        };
    }
}
