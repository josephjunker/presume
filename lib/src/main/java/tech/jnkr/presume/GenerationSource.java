package tech.jnkr.presume;

import tech.jnkr.presume.generators.*;

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

    <T> T call(Generator<T> generator);
}
