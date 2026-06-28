package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.SourceDepletedException;
import tech.jnkr.presume.utilities.RoseTree;

import java.util.List;

public class ReplayingSource implements GenerationSource {

    private final RoseTree<List<DrawAtom>> history;
    private int index;
    private int childIndex;

    private final BooleanGenerator booleanGenerator = new BooleanGenerator(this::getAtom);
    private final IntegerGenerator integerGenerator = new IntegerGenerator(this::getAtom);
    private final LongGenerator longGenerator = new LongGenerator(this::getAtom);
    private final FloatGenerator floatGenerator = new FloatGenerator(this::getAtom);
    private final DoubleGenerator doubleGenerator = new DoubleGenerator(this::getAtom);

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
        RoseTree<List<DrawAtom>> childHistory = history.children().get(childIndex);
        childIndex++;
        if (childHistory == null) throw new SourceDepletedException();
        ReplayingSource childSource = new ReplayingSource(childHistory);
        return generator.gen(childSource);
    }
}
