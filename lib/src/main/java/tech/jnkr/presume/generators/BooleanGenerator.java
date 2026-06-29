package tech.jnkr.presume.generators;

public interface BooleanGenerator {
    BooleanGenerator shrinkTowards(boolean target);

    Boolean gen();
}
