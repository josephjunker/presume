package tech.jnkr.presume.internal.utilities;

import java.util.function.Function;
import java.util.stream.Stream;

public sealed interface Maybe<T> permits Just, Nothing {
    static <T> Maybe<T> of(T value) {
        return new Just<>(value);
    }

    static <T> Maybe<T> empty() {
        return new Nothing<>();
    }

    default <U> Maybe<U> map(Function<T, U> fn) {
        return switch (this) {
            case Nothing() -> new Nothing<>();
            case Just(T value) -> new Just<>(fn.apply(value));
        };
    }

    default <U> Maybe<U> chain(Function<T, Maybe<U>> fn) {
        return switch (this) {
            case Nothing() -> new Nothing<>();
            case Just(T value) -> fn.apply(value);
        };
    }

    static <T> Stream<T> justStream(Stream<Maybe<T>> stream) {
        return stream.takeWhile(value -> value instanceof Just<T>)
                .map(
                        maybe -> {
                            Just<T> just = (Just<T>) maybe;
                            return (T) just.value();
                        });
    }
}

record Just<T>(T value) implements Maybe<T> {}

record Nothing<T>() implements Maybe<T> {}
