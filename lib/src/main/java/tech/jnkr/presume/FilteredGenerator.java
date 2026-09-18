package tech.jnkr.presume;

import tech.jnkr.presume.exceptions.FilterConditionNotSatisfiableException;

import java.util.function.Predicate;

public class FilteredGenerator<T> extends AbstractGenerator<T> {
    private final AbstractGenerator<T> innerGen;
    private final Predicate<T> predicate;

    public FilteredGenerator(AbstractGenerator<T> innerGen, Predicate<T> predicate) {
        this.innerGen = innerGen;
        this.predicate = predicate;
    }

    @Override
    protected T gen(GenerationSource source) {
        // TODO: should do something STM-like here, where we attempt a generation
        // and then roll back the state of the source if the generation fails,
        // meaning we won't bloat our history with atoms that lead to a predicate failure.
        for (int i = 1; i < 10_000; i++) {
            T current = innerGen.gen(source);
            if (predicate.test(current)) return current;
        }

        throw new FilterConditionNotSatisfiableException(10_000);
    }
}
