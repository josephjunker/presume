package tech.jnkr.presume;

public abstract class AbstractGenerator<T> {
    protected abstract T gen(GenerationSource source);

    T internalGen(GenerationSource source) {
        return gen(source);
    }
}
