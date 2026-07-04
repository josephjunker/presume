package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.SourceDepletedException;
import tech.jnkr.presume.generators.*;
import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.generators.*;
import tech.jnkr.presume.internal.utilities.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReplayingSource implements GenerationSource {

    private final RoseTree<List<DrawAtom>> history;
    private final List<ReplayingSource> children;

    private Maybe<ListZipper<DrawAtom>> atomCursor;
    private Maybe<ListZipper<RoseTree<List<DrawAtom>>>> childCursor;

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
        this.atomCursor = ListZipper.from(ImmutableList.fromList(history.value));
        this.childCursor = ListZipper.from(history.children);
    }

    private DrawAtom getAtom() {
        return switch (atomCursor) {
            case Nothing():
                throw new SourceDepletedException();
            case Just(ListZipper<DrawAtom> zipper):
                {
                    atomCursor = zipper.right();
                    yield zipper.focus();
                }
        };
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
        switch (childCursor) {
            case Nothing():
                throw new SourceDepletedException();
            case Just(ListZipper<RoseTree<List<DrawAtom>>> zipper):
                {
                    ReplayingSource childSource = new ReplayingSource(zipper.focus());
                    childCursor = zipper.right();
                    children.add(childSource);
                    return generator.gen(childSource);
                }
        }
    }

    RoseTree<ImmutableList<DrawAtom>> getTruncatedHistory() {
        return new RoseTree<>(
                atomCursor.map(ListZipper::leftSublist).orDefault(ImmutableList.empty()),
                // TODO: implement proper collector
                ImmutableList.fromList(
                        children.stream()
                                .map(ReplayingSource::getTruncatedHistory)
                                .collect(Collectors.toList())));
    }
}
