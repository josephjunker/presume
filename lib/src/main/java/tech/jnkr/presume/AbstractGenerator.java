package tech.jnkr.presume;

import tech.jnkr.presume.generators.GenerationSource;

public abstract class AbstractGenerator<T> {
    protected abstract T gen(GenerationSource source);

    T internalGen(GenerationSource source) {
        return gen(source);
    }
}
