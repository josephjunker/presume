package tech.jnkr.presume.generators;

import tech.jnkr.presume.AbstractGenerator;

public interface GenerationSource {
    boolean getBoolean();

    BooleanGenerator booleanGen();

    int getInteger();

    IntegerGenerator integerGen();

    long getLong();

    LongGenerator longGen();

    float getFloat();

    FloatGenerator floatGen();

    double getDouble();

    DoubleGenerator doubleGen();

    <T> T call(AbstractGenerator<T> generator);
}
