package tech.jnkr.presume;

public abstract class Generator<T> {
    protected abstract T gen(GenerationSource source);

    T internalGen(GenerationSource source) {
        return gen(source);
    }
}
