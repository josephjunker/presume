package tech.jnkr.presume;

public interface GenerationSource {
    boolean getBoolean();

    BooleanGenerator booleanGen();

    int getInteger();

    IntegerGenerator integerGen();

    float getFloat();

    FloatGenerator floatGen();

    double getDouble();

    DoubleGenerator doubleGen();

    long getLong();

    LongGenerator longGen();

    <T> T call(Generator<T> generator);
}
