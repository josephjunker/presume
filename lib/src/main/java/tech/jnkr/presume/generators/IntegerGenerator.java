package tech.jnkr.presume.generators;

public interface IntegerGenerator {
    IntegerGenerator withMinimum(int minimum);

    IntegerGenerator withMaximum(int maximum);

    IntegerGenerator shrinkingTowards(int approaching);

    Integer gen();
}
