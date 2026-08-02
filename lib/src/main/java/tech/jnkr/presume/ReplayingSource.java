package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.SourceDepletedException;
import tech.jnkr.presume.generators.*;
import tech.jnkr.presume.internal.History;
import tech.jnkr.presume.internal.Trace;
import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.generators.*;
import tech.jnkr.presume.internal.shrinking.Down;
import tech.jnkr.presume.internal.shrinking.Right;
import tech.jnkr.presume.internal.shrinking.TraceEntry;
import tech.jnkr.presume.internal.utilities.*;

import java.util.ArrayList;

class ReplayingSource implements GenerationSource {

    private final MutableRoseTree<ArrayList<TraceEntry>> trace;

    private Maybe<ListZipper<DrawAtom>> atomCursor;
    private Maybe<ListZipper<RoseTree<ArrayList<DrawAtom>>>> childCursor;

    private final ConcreteBooleanGenerator booleanGenerator =
            new ConcreteBooleanGenerator(this::getAtom);
    private final ConcreteIntegerGenerator integerGenerator =
            new ConcreteIntegerGenerator(this::getAtom);
    private final ConcreteLongGenerator longGenerator = new ConcreteLongGenerator(this::getAtom);
    private final ConcreteFloatGenerator floatGenerator = new ConcreteFloatGenerator(this::getAtom);
    private final ConcreteDoubleGenerator doubleGenerator =
            new ConcreteDoubleGenerator(this::getAtom);

    public ReplayingSource(History history, MutableRoseTree<ArrayList<TraceEntry>> trace) {
        this.atomCursor = ListZipper.from(ImmutableList.fromList(history.contents.value));
        this.childCursor = ListZipper.from(history.contents.children);
        this.trace = trace;
    }

    public ReplayingSource(History history) {
        this.atomCursor = ListZipper.from(ImmutableList.fromList(history.contents.value));
        this.childCursor = ListZipper.from(history.contents.children);
        this.trace = new MutableRoseTree<>(new ArrayList<>());
    }

    private DrawAtom getAtom() {
        return switch (atomCursor) {
            case Nothing():
                throw new SourceDepletedException();
            case Just(ListZipper<DrawAtom> zipper):
                {
                    atomCursor = zipper.right();
                    DrawAtom result = zipper.focus();
                    trace.value.add(new Right(result));
                    yield result;
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
    public <T> T call(AbstractGenerator<T> generator) {
        switch (childCursor) {
            case Nothing():
                throw new SourceDepletedException();
            case Just(ListZipper<RoseTree<ArrayList<DrawAtom>>> zipper):
                {
                    MutableRoseTree<ArrayList<TraceEntry>> childTrace =
                            new MutableRoseTree<>(new ArrayList<>());
                    trace.value.add(new Down());
                    trace.children.add(childTrace);

                    // Q: We have cases where a `Down` in the parent isn't followed by a `Right`
                    // in the child. Do we need to add it here or elsewhere?
                    // A: It *should* be happening in getAtom()

                    ReplayingSource childSource =
                            new ReplayingSource(new History(zipper.focus()), childTrace);
                    childCursor = zipper.right();

                    return generator.internalGen(childSource);
                }
        }
    }

    public Trace finalTrace() {
        return new Trace(trace.map(ImmutableList::fromList).toImmutable());
    }
}
