package tech.jnkr.presume;

import tech.jnkr.presume.internal.atoms.AtomSource;
import tech.jnkr.presume.internal.atoms.DrawAtom;

class PureSource implements GenerationSource {

    private final AtomSource atomSource = new AtomSource();

    private final BooleanGenerator booleanGenerator;
    private final IntegerGenerator integerGenerator;
    private final LongGenerator longGenerator;
    private final FloatGenerator floatGenerator;
    private final DoubleGenerator doubleGenerator;
    private final CharacterGenerator characterGenerator;

    public PureSource() {
        booleanGenerator = new BooleanGenerator(this::getAtom);
        integerGenerator = new IntegerGenerator(this::getAtom);
        longGenerator = new LongGenerator(this::getAtom);
        floatGenerator = new FloatGenerator(this::getAtom);
        doubleGenerator = new DoubleGenerator(this::getAtom);
        characterGenerator = new CharacterGenerator(this::getAtom);
    }

    @Override
    public boolean getBoolean() {
        return booleanGenerator.gen(this);
    }

    @Override
    public BooleanGenerator booleanGen() {
        return booleanGenerator;
    }

    @Override
    public int getInteger() {
        return integerGenerator.gen(this);
    }

    @Override
    public IntegerGenerator integerGen() {
        return integerGenerator;
    }

    @Override
    public long getLong() {
        return longGenerator.genPrimitive();
    }

    @Override
    public LongGenerator longGen() {
        return longGenerator;
    }

    @Override
    public float getFloat() {
        return floatGenerator.genPrimitive();
    }

    @Override
    public FloatGenerator floatGen() {
        return floatGenerator;
    }

    @Override
    public double getDouble() {
        return doubleGenerator.genPrimitive();
    }

    @Override
    public DoubleGenerator doubleGen() {
        return doubleGenerator;
    }

    @Override
    public char getCharacter() {
        return characterGenerator.genPrimitive();
    }

    @Override
    public CharacterGenerator characterGen() {
        return characterGenerator;
    }

    @Override
    public <T> T call(AbstractGenerator<T> generator) {
        return generator.internalGen(this);
    }

    DrawAtom getAtom() {
        return atomSource.getAtom();
    }
}
