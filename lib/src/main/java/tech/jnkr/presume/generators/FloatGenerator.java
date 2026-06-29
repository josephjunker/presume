package tech.jnkr.presume.generators;

public interface FloatGenerator {
    FloatGenerator withMinimum(float minimum);

    FloatGenerator withMaximum(float maximum);

    FloatGenerator shrinkingTowards(float approaching);

    FloatGenerator allowNaN();

    FloatGenerator disallowNaN();

    FloatGenerator allowInfinity();

    FloatGenerator disallowInfinity();

    Float gen();
}
