package tech.jnkr.presume.internal.generators;

import tech.jnkr.presume.generators.GenerationSource;

public abstract class InternalGenerator<T> {
    protected abstract T gen(GenerationSource source);

    T internalGen(GenerationSource source) {
        return gen(source);
    }
}
