package tech.jnkr.presume.generators;

public interface DoubleGenerator {
    DoubleGenerator withMinimum(double minimum);

    DoubleGenerator withMaximum(double maximum);

    DoubleGenerator shrinkingTowards(double approaching);

    DoubleGenerator allowNaN();

    DoubleGenerator disallowNaN();

    DoubleGenerator allowInfinity();

    DoubleGenerator disallowInfinity();

    Double gen();
}
