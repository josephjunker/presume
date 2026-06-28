package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.SourceDepletedException;
import tech.jnkr.presume.utilities.RoseTree;

import java.util.List;

public class ReplayingSource implements GenerationSource {

    private final RoseTree<List<DrawAtom>> history;
    private int index;
    private int childIndex;

    private final BooleanProducer booleanProducer = new BooleanProducer(this::getAtom);
    private final IntegerProducer integerProducer = new IntegerProducer(this::getAtom);
    private final LongProducer longProducer = new LongProducer(this::getAtom);
    private final FloatProducer floatProducer = new FloatProducer(this::getAtom);
    private final DoubleProducer doubleProducer = new DoubleProducer(this::getAtom);

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
        RoseTree<List<DrawAtom>> childHistory = history.children().get(childIndex);
        childIndex++;
        if (childHistory == null) throw new SourceDepletedException();
        ReplayingSource childSource = new ReplayingSource(childHistory);
        return generator.gen(childSource);
    }
}
