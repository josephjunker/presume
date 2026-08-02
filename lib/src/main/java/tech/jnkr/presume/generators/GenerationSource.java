package tech.jnkr.presume.generators;

import tech.jnkr.presume.AbstractGenerator;

public interface GenerationSource {
    boolean getBoolean();

    BooleanGenerator booleanGen();

    int getInteger();

    default int getInteger(int minimum, int maximum) {
        return integerGen().withMinimum(minimum).withMaximum(maximum).gen();
    }

    IntegerGenerator integerGen();

    long getLong();

    default long getLong(long minimum, long maximum) {
        return longGen().withMinimum(minimum).withMaximum(maximum).gen();
    }

    LongGenerator longGen();

    float getFloat();

    default float getFloat(float minimum, float maximum) {
        return floatGen().withMinimum(minimum).withMaximum(maximum).gen();
    }

    default float getBoringFloat() {
        return floatGen().disallowNaN().disallowInfinity().gen();
    }

    default float getBoringFloat(float minimum, float maximum) {
        return floatGen()
                .withMinimum(minimum)
                .withMaximum(maximum)
                .disallowNaN()
                .disallowInfinity()
                .gen();
    }

    FloatGenerator floatGen();

    double getDouble();

    default double getDouble(double minimum, double maximum) {
        return doubleGen().withMinimum(minimum).withMaximum(maximum).gen();
    }

    default double getBoringDouble() {
        return doubleGen().disallowNaN().disallowInfinity().gen();
    }

    default double getBoringDouble(double minimum, double maximum) {
        return doubleGen()
                .withMinimum(minimum)
                .withMaximum(maximum)
                .disallowNaN()
                .disallowInfinity()
                .gen();
    }

    DoubleGenerator doubleGen();

    <T> T call(AbstractGenerator<T> generator);
}
