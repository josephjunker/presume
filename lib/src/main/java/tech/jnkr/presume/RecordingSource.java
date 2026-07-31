package tech.jnkr.presume;

import tech.jnkr.presume.generators.*;
import tech.jnkr.presume.internal.atoms.AtomSource;
import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.generators.*;
import tech.jnkr.presume.internal.utilities.ImmutableList;
import tech.jnkr.presume.internal.utilities.RoseTree;

import java.util.ArrayList;
import java.util.List;

class RecordingSource implements GenerationSource {

    private final AtomSource atomSource = new AtomSource();
    private final List<DrawAtom> history = new ArrayList<>();
    private final List<RecordingSource> children = new ArrayList<>();

    private final ConcreteBooleanGenerator booleanGenerator;
    private final ConcreteIntegerGenerator integerGenerator;
    private final ConcreteLongGenerator longGenerator;
    private final ConcreteFloatGenerator floatGenerator;
    private final ConcreteDoubleGenerator doubleGenerator;

    public RecordingSource() {
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

    public <T> T call(Generator<T> generator) {
        RecordingSource childSource = new RecordingSource();
        children.add(childSource);
        return generator.gen(childSource);
    }

    DrawAtom getAtom() {
        DrawAtom atom = atomSource.getAtom();
        history.add(atom);
        return atom;
    }

    public RoseTree<ImmutableList<DrawAtom>> getHistory() {
        // TODO: stack safety.
        // This should also return History instead of a bare data structure
        return new RoseTree<>(
                ImmutableList.fromList(history),
                ImmutableList.fromList(children).map(RecordingSource::getHistory));
    }
}
