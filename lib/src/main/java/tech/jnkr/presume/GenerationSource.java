package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.InvalidGeneratorException;

import java.util.List;

public interface GenerationSource {
    boolean getBoolean();

    BooleanGenerator booleanGen();

    default boolean getWeightedBoolean(float trueRatio) {
        int draw = getInteger(0, 1000);
        return draw <= trueRatio * 1000f;
    }

    int getInteger();

    default int getInteger(int minimum, int maximum) {
        return integerGen().withMinimum(minimum).withMaximum(maximum).gen(this);
    }

    IntegerGenerator integerGen();

    long getLong();

    default long getLong(long minimum, long maximum) {
        return longGen().withMinimum(minimum).withMaximum(maximum).genPrimitive();
    }

    LongGenerator longGen();

    float getFloat();

    default float getFloat(float minimum, float maximum) {
        return floatGen().withMinimum(minimum).withMaximum(maximum).genPrimitive();
    }

    default float getBoringFloat() {
        return floatGen().disallowNaN().disallowInfinity().genPrimitive();
    }

    default float getBoringFloat(float minimum, float maximum) {
        return floatGen()
                .withMinimum(minimum)
                .withMaximum(maximum)
                .disallowNaN()
                .disallowInfinity()
                .genPrimitive();
    }

    FloatGenerator floatGen();

    double getDouble();

    default double getDouble(double minimum, double maximum) {
        return doubleGen().withMinimum(minimum).withMaximum(maximum).genPrimitive();
    }

    default double getBoringDouble() {
        return doubleGen().disallowNaN().disallowInfinity().genPrimitive();
    }

    default double getBoringDouble(double minimum, double maximum) {
        return doubleGen()
                .withMinimum(minimum)
                .withMaximum(maximum)
                .disallowNaN()
                .disallowInfinity()
                .genPrimitive();
    }

    DoubleGenerator doubleGen();

    <T> T call(AbstractGenerator<T> generator);

    default <T> T oneOfLeftBiased(List<AbstractGenerator<T>> generators) {
        if (generators.isEmpty())
            throw new InvalidGeneratorException(
                    "Tried to call oneOf without providing any generators");

        int index = getInteger(0, generators.size() - 1);

        return call(generators.get(index));
    }
}
