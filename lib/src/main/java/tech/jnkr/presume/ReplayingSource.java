package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.SourceDepletedException;
import tech.jnkr.presume.utilities.RoseTree;

import java.util.List;

public class ReplayingSource implements GenerationSource {

    private final RoseTree<List<DrawAtom>> history;
    private int index;
    private int childIndex;

    private final BooleanProducer booleanProducer = new BooleanProducer();
    private final IntegerProducer integerProducer = new IntegerProducer();
    private final LongProducer longProducer = new LongProducer();
    private final FloatProducer floatProducer = new FloatProducer();
    private final DoubleProducer doubleProducer = new DoubleProducer();

    ReplayingSource(RoseTree<List<DrawAtom>> history) {
        this.history = history;
        this.index = 0;
        this.childIndex = 0;
    }

    private DrawAtom getAtom() {
        DrawAtom atom = history.value().get(index);
        if (atom == null) throw new SourceDepletedException();
        index++;
        return atom;
    }

    public boolean getBoolean() {
        return booleanProducer.produce(getAtom());
    }

    public int getInteger() {
        return integerProducer.produce(getAtom());
    }

    public float getFloat() {
        return floatProducer.produce(getAtom());
    }

    public double getDouble() {
        return doubleProducer.produce(getAtom());
    }

    public long getLong() {
        return longProducer.produce(getAtom());
    }

    public <T> T call(Generator<T> generator) {
        RoseTree<List<DrawAtom>> childHistory = history.children().get(childIndex);
        childIndex++;
        if (childHistory == null) throw new SourceDepletedException();
        ReplayingSource childSource = new ReplayingSource(childHistory);
        return generator.gen(childSource);
    }
}
