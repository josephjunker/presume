package tech.jnkr.presume;

public interface GenerationSource {
    boolean getBoolean();

    BooleanProducer booleanGen();

    int getInteger();

    IntegerProducer integerGen();

    float getFloat();

    FloatProducer floatGen();

    double getDouble();

    DoubleProducer doubleGen();

    long getLong();

    LongProducer longGen();

    <T> T call(Generator<T> generator);
}
