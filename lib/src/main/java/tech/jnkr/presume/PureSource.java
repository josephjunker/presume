package tech.jnkr.presume;

import tech.jnkr.presume.generators.*;
import tech.jnkr.presume.internal.atoms.AtomSource;
import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.generators.*;

class PureSource implements GenerationSource {

    private final AtomSource atomSource = new AtomSource();

    private final ConcreteBooleanGenerator booleanGenerator;
    private final ConcreteIntegerGenerator integerGenerator;
    private final ConcreteLongGenerator longGenerator;
    private final ConcreteFloatGenerator floatGenerator;
    private final ConcreteDoubleGenerator doubleGenerator;

    public PureSource() {
        booleanGenerator = new ConcreteBooleanGenerator(this::getAtom);
        integerGenerator = new ConcreteIntegerGenerator(this::getAtom);
        longGenerator = new ConcreteLongGenerator(this::getAtom);
        floatGenerator = new ConcreteFloatGenerator(this::getAtom);
        doubleGenerator = new ConcreteDoubleGenerator(this::getAtom);
    }

    public boolean getBoolean() {
        return booleanGenerator.gen();
    }

    public BooleanGenerator booleanGen() {
        return booleanGenerator;
    }

    public int getInteger() {
        return integerGenerator.gen();
    }

    public IntegerGenerator integerGen() {
        return integerGenerator;
    }

    public long getLong() {
        return longGenerator.gen();
    }

    public LongGenerator longGen() {
        return longGenerator;
    }

    public float getFloat() {
        return floatGenerator.gen();
    }

    public FloatGenerator floatGen() {
        return floatGenerator;
    }

    public double getDouble() {
        return doubleGenerator.gen();
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
