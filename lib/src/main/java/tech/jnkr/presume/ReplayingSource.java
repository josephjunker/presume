package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.SourceDepletedException;
import tech.jnkr.presume.utilities.RoseTree;

import java.util.List;

public class ReplayingSource implements GenerationSource {
    private final RoseTree<List<PrimitiveDraw>> history;
    private int index;

    ReplayingSource(RoseTree<List<PrimitiveDraw>> history) {
        this.history = history;
        index = 0;
    }

    @Override
    public boolean genBoolean() {
        if (index > history.value().size()) {
            throw new SourceDepletedException();
        }

        PrimitiveDraw draw = history.value().get(index);
        return switch (draw) {
            case BooleanDraw(boolean value) -> value;
            case IntDraw(int value) -> Math.abs(value) > 0;
            case FloatDraw(float value) -> Math.abs(value) > 0.01;
            case DoubleDraw(double value) -> Math.abs(value) > 0.01;
            case LongDraw(long value) -> Math.abs(value) > 0;
        };
    }

    @Override
    public int genInt() {
        if (index > history.value().size()) {
            throw new SourceDepletedException();
        }

        PrimitiveDraw draw = history.value().get(index);
        return switch (draw) {
            case BooleanDraw(boolean value) -> value ? 0 : 1;
            case IntDraw(int value) -> Math.abs(value) > 0;
            case FloatDraw(float value) -> Math.abs(value) > 0.01;
            case DoubleDraw(double value) -> Math.abs(value) > 0.01;
            case LongDraw(long value) -> Math.abs(value) > 0;
        };
    }

    @Override
    public float genFloat() {
        return 0;
    }

    @Override
    public double genDouble() {
        return 0;
    }

    @Override
    public long genLong() {
        return 0;
    }

    @Override
    public <T> T call(Generator<T> generator) {
        return null;
    }
}
