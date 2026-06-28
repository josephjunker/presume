package tech.jnkr.presume;

import tech.jnkr.presume.utilities.RoseTree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class RecordingSource implements GenerationSource {

    private final AtomSource atomSource = new AtomSource();
    private final List<DrawAtom> history = new ArrayList<>();
    private final List<RecordingSource> children = new ArrayList<>();

    private final BooleanProducer booleanProducer;
    private final IntegerProducer integerProducer;
    private final LongProducer longProducer;
    private final FloatProducer floatProducer;
    private final DoubleProducer doubleProducer;

    public RecordingSource() {
        booleanProducer = new BooleanProducer(this::getAtom);
        integerProducer = new IntegerProducer(this::getAtom);
        longProducer = new LongProducer(this::getAtom);
        floatProducer = new FloatProducer(this::getAtom);
        doubleProducer = new DoubleProducer(this::getAtom);
    }

    public boolean getBoolean() {
        return booleanProducer.gen();
    }

    public BooleanProducer booleanGen() {
        return booleanProducer;
    }

    public int getInteger() {
        return integerProducer.gen();
    }

    public IntegerProducer integerGen() {
        return integerProducer;
    }

    public long getLong() {
        return longProducer.gen();
    }

    public LongProducer longGen() {
        return longProducer;
    }

    public float getFloat() {
        return floatProducer.gen();
    }

    public FloatProducer floatGen() {
        return floatProducer;
    }

    public double getDouble() {
        return doubleProducer.gen();
    }

    public DoubleProducer doubleGen() {
        return doubleProducer;
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
