package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.SourceDepletedException;
import tech.jnkr.presume.internal.History;
import tech.jnkr.presume.internal.Trace;
import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.shrinking.Down;
import tech.jnkr.presume.internal.shrinking.Right;
import tech.jnkr.presume.internal.shrinking.TraceEntry;
import tech.jnkr.presume.internal.utilities.*;

import java.util.ArrayList;

class ReplayingSource implements GenerationSource {

    private final MutableRoseTree<ArrayList<TraceEntry>> trace;

    private Maybe<ListZipper<DrawAtom>> atomCursor;
    private Maybe<ListZipper<RoseTree<ArrayList<DrawAtom>>>> childCursor;

    private final BooleanGenerator booleanGenerator = new BooleanGenerator(this::getAtom);
    private final IntegerGenerator integerGenerator = new IntegerGenerator(this::getAtom);
    private final LongGenerator longGenerator = new LongGenerator(this::getAtom);
    private final FloatGenerator floatGenerator = new FloatGenerator(this::getAtom);
    private final DoubleGenerator doubleGenerator = new DoubleGenerator(this::getAtom);

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
                    DrawAtom result = zipper.focus();
                    atomCursor = zipper.right();

                    trace.value.add(new Right(result));
                    yield result;
                }
        };
    }

    @Override
    public boolean getBoolean() {
        return booleanGenerator.gen(this);
    }

    @Override
    public BooleanGenerator booleanGen() {
        return booleanGenerator;
    }

    @Override
    public int getInteger() {
        return integerGenerator.gen(this);
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

                    ReplayingSource childSource =
                            new ReplayingSource(new History(zipper.focus()), childTrace);
                    childCursor = zipper.right();

                    return generator.gen(childSource);
                }
        }
    }

    Trace finalTrace() {
        return new Trace(trace.map(ImmutableList::fromList).toImmutable());
    }
}
