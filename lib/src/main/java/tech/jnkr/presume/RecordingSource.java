package tech.jnkr.presume;

import tech.jnkr.presume.utilities.RoseTree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class RecordingSource implements GenerationSource {

    private final AtomSource atomSource = new AtomSource();
    private final List<DrawAtom> history = new ArrayList<>();
    private final List<RecordingSource> children = new ArrayList<>();

    private final BooleanGenerator booleanGenerator;
    private final IntegerGenerator integerGenerator;
    private final LongGenerator longGenerator;
    private final FloatGenerator floatGenerator;
    private final DoubleGenerator doubleGenerator;

    public RecordingSource() {
        booleanGenerator = new BooleanGenerator(this::getAtom);
        integerGenerator = new IntegerGenerator(this::getAtom);
        longGenerator = new LongGenerator(this::getAtom);
        floatGenerator = new FloatGenerator(this::getAtom);
        doubleGenerator = new DoubleGenerator(this::getAtom);
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

    public RoseTree<List<DrawAtom>> getHistory() {
        // TODO: stack safety
        return new RoseTree<>(
                this.history,
                this.children.stream()
                        .map(RecordingSource::getHistory)
                        .collect(Collectors.toList()));
    }
}
