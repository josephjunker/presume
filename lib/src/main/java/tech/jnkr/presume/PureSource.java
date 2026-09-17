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

    public PureSource() {
        booleanGenerator = new BooleanGenerator(this::getAtom);
        integerGenerator = new IntegerGenerator(this::getAtom);
        longGenerator = new LongGenerator(this::getAtom);
        floatGenerator = new FloatGenerator(this::getAtom);
        doubleGenerator = new DoubleGenerator(this::getAtom);
    }

    public boolean getBoolean() {
        return booleanGenerator.gen(this);
    }

    public BooleanGenerator booleanGen() {
        return booleanGenerator;
    }

    public int getInteger() {
        return integerGenerator.gen(this);
    }

    public IntegerGenerator integerGen() {
        return integerGenerator;
    }

    public long getLong() {
        return longGenerator.genPrimitive();
    }

    public LongGenerator longGen() {
        return longGenerator;
    }

    public float getFloat() {
        return floatGenerator.genPrimitive();
    }

    public FloatGenerator floatGen() {
        return floatGenerator;
    }

    public double getDouble() {
        return doubleGenerator.genPrimitive();
    }

    public DoubleGenerator doubleGen() {
        return doubleGenerator;
    }

    public <T> T call(AbstractGenerator<T> generator) {
        return generator.internalGen(this);
    }

    DrawAtom getAtom() {
        return atomSource.getAtom();
    }
}
