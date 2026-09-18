package tech.jnkr.presume;

import java.util.function.Function;

public class MappedGenerator<T> extends AbstractGenerator<T> {
    private final Function<GenerationSource, T> fn;

    MappedGenerator(Function<GenerationSource, T> fn) {
        this.fn = fn;
    }

    @Override
    protected T gen(GenerationSource source) {
        return fn.apply(source);
    }
}
