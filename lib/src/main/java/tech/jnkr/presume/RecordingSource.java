package tech.jnkr.presume;

import tech.jnkr.presume.internal.atoms.AtomSource;
import tech.jnkr.presume.internal.atoms.DrawAtom;
import tech.jnkr.presume.internal.utilities.ImmutableList;
import tech.jnkr.presume.internal.utilities.RoseTree;

import java.util.ArrayList;
import java.util.List;

class RecordingSource implements GenerationSource {

    private final AtomSource atomSource;
    private final List<DrawAtom> history;
    private final List<RecordingSource> children;

    private final BooleanGenerator booleanGenerator;
    private final IntegerGenerator integerGenerator;
    private final LongGenerator longGenerator;
    private final FloatGenerator floatGenerator;
    private final DoubleGenerator doubleGenerator;
    private final CharacterGenerator characterGenerator;

    public RecordingSource() {
        atomSource = new AtomSource();
        history = new ArrayList<>();
        children = new ArrayList<>();

        booleanGenerator = new BooleanGenerator(this::getAtom);
        integerGenerator = new IntegerGenerator(this::getAtom);
        longGenerator = new LongGenerator(this::getAtom);
        floatGenerator = new FloatGenerator(this::getAtom);
        doubleGenerator = new DoubleGenerator(this::getAtom);
        characterGenerator = new CharacterGenerator(this::getAtom);
    }

    private RecordingSource(
            AtomSource atomSource, List<DrawAtom> history, List<RecordingSource> children) {
        this.atomSource = atomSource;
        this.history = history;
        this.children = children;

        booleanGenerator = new BooleanGenerator(this::getAtom);
        integerGenerator = new IntegerGenerator(this::getAtom);
        longGenerator = new LongGenerator(this::getAtom);
        floatGenerator = new FloatGenerator(this::getAtom);
        doubleGenerator = new DoubleGenerator(this::getAtom);
        characterGenerator = new CharacterGenerator(this::getAtom);
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
        return longGenerator.genPrimitive();
    }

    @Override
    public LongGenerator longGen() {
        return longGenerator;
    }

    @Override
    public float getFloat() {
        return floatGenerator.genPrimitive();
    }

    @Override
    public FloatGenerator floatGen() {
        return floatGenerator;
    }

    @Override
    public double getDouble() {
        return doubleGenerator.genPrimitive();
    }

    @Override
    public DoubleGenerator doubleGen() {
        return doubleGenerator;
    }

    @Override
    public char getCharacter() {
        return characterGenerator.genPrimitive();
    }

    @Override
    public CharacterGenerator characterGen() {
        return characterGenerator;
    }

    @Override
    public <T> T call(AbstractGenerator<T> generator) {
        RecordingSource childSource = new RecordingSource();
        children.add(childSource);
        return generator.internalGen(childSource);
    }

    DrawAtom getAtom() {
        DrawAtom atom = atomSource.getAtom();
        history.add(atom);
        return atom;
    }

    RoseTree<ImmutableList<DrawAtom>> getHistory() {
        // TODO: stack safety.
        // This should also return History instead of a bare data structure
        return new RoseTree<>(
                ImmutableList.fromList(history),
                ImmutableList.fromList(children).map(RecordingSource::getHistory));
    }
}
