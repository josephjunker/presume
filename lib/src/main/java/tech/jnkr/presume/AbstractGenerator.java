package tech.jnkr.presume;

import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractGenerator<T> {
    protected abstract T gen(GenerationSource source);

    T internalGen(GenerationSource source) {
        return gen(source);
    }

    public <U> MappedGenerator<U> map(Function<T, U> mapper) {
        return new MappedGenerator<>(source -> mapper.apply(gen(source)));
    }

    public <U> MappedGenerator<U> flatMap(Function<T, AbstractGenerator<U>> mapper) {
        return new MappedGenerator<>(source -> source.call(mapper.apply(gen(source))));
    }

    public FilteredGenerator<T> filter(Predicate<T> predicate) {
        return new FilteredGenerator<>(this, predicate);
    }
}
