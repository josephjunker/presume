package tech.jnkr.presume.internal.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public sealed interface ImmutableList<T> permits Cons, Nil {

    static <T> ImmutableList<T> empty() {
        return new Nil<>();
    }

    static <T> ImmutableList<T> of(T value) {
        return new Cons<>(value, new Nil<>());
    }

    static <T> ImmutableList<T> fromList(List<T> list) {
        ImmutableList<T> result = new Nil<>();

        for (T item : list) {
            result = new Cons<>(item, result);
        }

        return result.reverse();
    }

    default ImmutableList<T> push(T value) {
        return new Cons<>(value, this);
    }

    default ImmutableList<T> pop() {
        return switch (this) {
            case Nil() -> this;
            case Cons(T head, ImmutableList<T> tail) -> tail;
        };
    }

    default ImmutableList<T> reverse() {
        ImmutableList<T> result = new Nil<>();
        ImmutableList<T> current = this;

        while (true) {
            switch (current) {
                case Nil():
                    return result;
                case Cons(T head, ImmutableList<T> tail):
                    {
                        result = new Cons<>(head, result);
                        current = tail;
                    }
            }
        }
    }

    default ImmutableList<T> concat(ImmutableList<T> other) {
        ImmutableList<T> currentThis = this.reverse();
        ImmutableList<T> result = other;

        while (true) {
            switch (currentThis) {
                case Nil():
                    return result;
                case Cons(T head, ImmutableList<T> tail):
                    {
                        result = new Cons<>(head, result);
                        currentThis = tail;
                    }
            }
        }
    }

    default Maybe<ListZipper<T>> toZipper() {
        return ListZipper.from(this);
    }

    default <U> ImmutableList<U> map(Function<T, U> fn) {
        ImmutableList<U> result = new Nil<>();
        ImmutableList<T> cursor = this;

        while (true) {
            switch (cursor) {
                case Nil():
                    return result.reverse();
                case Cons(T head, ImmutableList<T> tail):
                    {
                        result = new Cons<>(fn.apply(head), result);
                        cursor = tail;
                    }
            }
        }
    }

    default <U> ImmutableList<U> filterMap(Function<T, Maybe<U>> fn) {
        ImmutableList<U> result = new Nil<>();
        var current = this;

        while (true) {
            switch (current) {
                case Nil():
                    {
                        return result.reverse();
                    }
                case Cons(T head, ImmutableList<T> tail):
                    {
                        switch (fn.apply(head)) {
                            case Nothing():
                                {
                                    break;
                                }
                            case Just(U value):
                                {
                                    result = result.push(value);
                                }
                        }

                        current = tail;
                    }
            }
        }
    }

    default ArrayList<T> toArrayList() {
        ImmutableList<T> current = this;
        ArrayList<T> result = new ArrayList<>();

        while (true) {
            switch (current) {
                case Nil():
                    {
                        return result;
                    }
                case Cons(T head, ImmutableList<T> tail):
                    {
                        result.add(head);
                        current = tail;
                    }
            }
        }
    }

    default void forEach(Consumer<T> fn) {
        ImmutableList<T> current = this;
        while (true) {
            switch (current) {
                case Nil():
                    return;
                case Cons(T head, ImmutableList<T> tail):
                    {
                        fn.accept(head);
                        current = tail;
                    }
            }
        }
    }

    default <U> U foldLeft(BiFunction<U, T, U> fn, U initial) {
        U acc = initial;
        ImmutableList<T> current = this;
        while (true) {
            switch (current) {
                case Nil():
                    return acc;
                case Cons(T head, ImmutableList<T> tail):
                    {
                        acc = fn.apply(acc, head);
                        current = tail;
                    }
            }
        }
    }

    default int size() {
        int length = 0;
        ImmutableList<T> current = this;
        while (true) {
            switch (current) {
                case Nil():
                    return length;
                case Cons(T head, ImmutableList<T> tail):
                    {
                        length++;
                        current = tail;
                    }
            }
        }
    }
}
