package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.SourceDepletedException;
import tech.jnkr.presume.generators.*;
import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.generators.*;
import tech.jnkr.presume.internal.utilities.RoseTree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReplayingSource implements GenerationSource {

    private final RoseTree<List<DrawAtom>> history;
    private final List<ReplayingSource> children;
    private int index;
    private int childIndex;

    private final ConcreteBooleanGenerator booleanGenerator =
            new ConcreteBooleanGenerator(this::getAtom);
    private final ConcreteIntegerGenerator integerGenerator =
            new ConcreteIntegerGenerator(this::getAtom);
    private final ConcreteLongGenerator longGenerator = new ConcreteLongGenerator(this::getAtom);
    private final ConcreteFloatGenerator floatGenerator = new ConcreteFloatGenerator(this::getAtom);
    private final ConcreteDoubleGenerator doubleGenerator =
            new ConcreteDoubleGenerator(this::getAtom);

    ReplayingSource(RoseTree<List<DrawAtom>> history) {
        this.history = history;
        this.children = new ArrayList<>();
        this.index = 0;
        this.childIndex = 0;
    }

    private DrawAtom getAtom() {
        DrawAtom atom = history.value().get(index);
        if (atom == null) throw new SourceDepletedException();
        index++;
        return atom;
    }

    @Override
    public boolean getBoolean() {
        return booleanGenerator.gen();
    }

    @Override
    public BooleanGenerator booleanGen() {
        return booleanGenerator;
    }

    @Override
    public int getInteger() {
        return integerGenerator.gen();
    }

    @Override
    public IntegerGenerator integerGen() {
        return integerGenerator;
    }

    @Override
    public long getLong() {
        return longGenerator.gen();
    }

    @Override
    public LongGenerator longGen() {
        return longGenerator;
    }

    @Override
    public float getFloat() {
        return floatGenerator.gen();
    }

    @Override
    public FloatGenerator floatGen() {
        return floatGenerator;
    }

    @Override
    public double getDouble() {
        return doubleGenerator.gen();
    }

    @Override
    public DoubleGenerator doubleGen() {
        return doubleGenerator;
    }

    @Override
    public <T> T call(Generator<T> generator) {
        RoseTree<List<DrawAtom>> childHistory = history.children().get(childIndex);
        childIndex++;
        if (childHistory == null) throw new SourceDepletedException();
        ReplayingSource childSource = new ReplayingSource(childHistory);
        children.add(childSource);
        return generator.gen(childSource);
    }

    RoseTree<List<DrawAtom>> getTruncatedHistory() {
        return new RoseTree<>(
                history.value().subList(0, childIndex),
                children.stream()
                        .map(ReplayingSource::getTruncatedHistory)
                        .collect(Collectors.toList()));
    }
}
