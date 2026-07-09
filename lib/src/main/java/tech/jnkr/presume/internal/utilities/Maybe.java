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

    static <T> Stream<T> filterJust(Stream<Maybe<T>> stream) {
        return stream.takeWhile(value -> value instanceof Just<T>)
                .map(
                        maybe -> {
                            Just<T> just = (Just<T>) maybe;
                            return (T) just.value();
                        });
    }

    static <T> ImmutableList<T> filterJust(ImmutableList<Maybe<T>> list) {
        ImmutableList<T> result = ImmutableList.empty();
        ImmutableList<Maybe<T>> cursor = list;

        while (true) {
            switch (cursor) {
                case Nil():
                    {
                        return result.reverse();
                    }
                case Cons(Maybe<T> head, ImmutableList<Maybe<T>> tail):
                    {
                        switch (head) {
                            case Nothing():
                                {
                                    break;
                                }
                            case Just(T value):
                                {
                                    result.push(value);
                                }
                        }

                        cursor = tail;
                    }
            }
        }
    }
}
