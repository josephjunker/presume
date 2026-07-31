package tech.jnkr.presume.internal.utilities;

import java.util.function.Function;

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

    default boolean isJust() {
        return this instanceof Just<T>;
    }

    default boolean isEmpty() {
        return this instanceof Nothing<T>;
    }

    default T orDefault(T defaultValue) {
        return switch (this) {
            case Nothing() -> defaultValue;
            case Just(T value) -> value;
        };
    }

    default T unwrapUnsafe() {
        return switch (this) {
            case Nothing():
                {
                    throw new RuntimeException("Attempted to unwrap a Nothing");
                }
            case Just(T value):
                {
                    yield value;
                }
        };
    }
}
