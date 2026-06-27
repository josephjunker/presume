package tech.jnkr.presume;

import tech.jnkr.presume.utilities.RoseTree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class RecordingSource implements GenerationSource {

    private final AtomSource atomSource = new AtomSource();
    private final List<DrawAtom> history = new ArrayList<>();
    private final List<RecordingSource> children = new ArrayList<>();

    private final BooleanProducer booleanProducer = new BooleanProducer();
    private final IntegerProducer integerProducer = new IntegerProducer();
    private final LongProducer longProducer = new LongProducer();
    private final FloatProducer floatProducer = new FloatProducer();
    private final DoubleProducer doubleProducer = new DoubleProducer();

    public boolean getBoolean() {
        DrawAtom atom = atomSource.getAtom();
        history.add(atom);
        return booleanProducer.produce(atom);
    }

    public int getInteger() {
        DrawAtom atom = atomSource.getAtom();
        history.add(atom);
        return integerProducer.produce(atom);
    }

    public long getLong() {
        DrawAtom atom = atomSource.getAtom();
        history.add(atom);
        return longProducer.produce(atom);
    }

    public float getFloat() {
        DrawAtom atom = atomSource.getAtom();
        history.add(atom);
        return floatProducer.produce(atom);
    }

    public double getDouble() {
        DrawAtom atom = atomSource.getAtom();
        history.add(atom);
        return doubleProducer.produce(atom);
    }

    public <T> T call(Generator<T> generator) {
        RecordingSource childSource = new RecordingSource();
        children.add(childSource);
        return generator.gen(childSource);
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
