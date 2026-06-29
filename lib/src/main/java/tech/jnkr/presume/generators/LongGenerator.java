package tech.jnkr.presume.generators;

public interface LongGenerator {
    LongGenerator withMinimum(long minimum);

    LongGenerator withMaximum(long maximum);

    LongGenerator shrinkingTowards(long target);

    Long gen();
}
