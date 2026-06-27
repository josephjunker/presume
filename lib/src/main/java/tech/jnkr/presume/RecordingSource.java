package tech.jnkr.presume;

import tech.jnkr.presume.utilities.RoseTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

class RecordingSource implements GenerationSource {

    private final List<PrimitiveDraw> history = new ArrayList<>();
    private final List<RecordingSource> children = new ArrayList<>();
    private final Random random = new Random();

    RecordingSource() {}

    public boolean genBoolean() {
        boolean result = random.nextBoolean();
        history.add(new BooleanDraw(result));
        return result;
    }

    public int genInt() {
        int result = random.nextInt();
        history.add(new IntDraw(result));
        return result;
    }

    public float genFloat() {
        float result = random.nextFloat();
        history.add(new FloatDraw(result));
        return result;
    }

    public double genDouble() {
        double result = random.nextDouble();
        history.add(new DoubleDraw(result));
        return result;
    }

    public long genLong() {
        long result = random.nextLong();
        history.add(new LongDraw(result));
        return result;
    }

    public <T> T call(Generator<T> generator) {
        RecordingSource childSource = new RecordingSource();
        children.add(childSource);
        return generator.gen(childSource);
    }

    public RoseTree<List<PrimitiveDraw>> getHistory() {
        // TODO: stack safety
        return new RoseTree<>(
                this.history,
                this.children.stream()
                        .map(RecordingSource::getHistory)
                        .collect(Collectors.toList()));
    }
}
