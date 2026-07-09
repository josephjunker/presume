package tech.jnkr.presume.internal.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public sealed interface ImmutableList<T> permits Cons, Nil {

    static <T> ImmutableList<T> empty() {
        return new Nil<>();
    }

    static <T> ImmutableList<T> fromList(List<T> list) {
        ImmutableList<T> result = new Nil<>();

        for (T item : list) {
            result = new Cons<>(item, result);
        }

        return result;
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
                        result = new Cons<>(head, tail);
                        current = tail;
                    }
            }
        }
    }

    default ImmutableList<T> concat(ImmutableList<T> other) {
        ImmutableList<T> currentThis = this;
        ImmutableList<T> currentOther = other;

        while (true) {
            switch (currentOther) {
                case Nil():
                    return currentThis;
                case Cons(T head, ImmutableList<T> tail):
                    {
                        currentThis = new Cons<>(head, currentThis);
                        currentOther = tail;
                    }
            }
        }
    }

    default Maybe<ListZipper<T>> toZipper() {
        return ListZipper.from(this);
    }

    default ImmutableList<ListZipper<T>> contexts() {
        ImmutableList<ListZipper<T>> results = new Nil<>();
        Maybe<ListZipper<T>> current = this.toZipper();

        while (true) {
            switch (current) {
                case Nothing():
                    return results.reverse();
                case Just(ListZipper<T> zipper):
                    {
                        results.push(zipper);
                        current = zipper.right();
                    }
            }
        }
    }

    default Stream<ListZipper<T>> contextsStream() {
        return Maybe.filterJust(
                Stream.iterate(
                        this.toZipper(),
                        (Maybe<ListZipper<T>> maybeZipper) ->
                                maybeZipper.chain(ListZipper::right)));
    }

    default ImmutableList<ImmutableList<T>> fillContexts(Function<T, T> fn) {
        return contexts().map((zipper) -> zipper.update(fn).toList());
    }

    default Stream<ImmutableList<T>> fillContextsStream(Function<T, T> fn) {
        return contextsStream().map((zipper) -> zipper.update(fn).toList());
    }

    default ImmutableList<ImmutableList<T>> fillContextsChain(Function<T, ImmutableList<T>> fn) {
        return contexts().chain((zipper) -> zipper.chainUpdate(fn));
    }

    default Stream<ImmutableList<T>> fillContextsChainStream(Function<T, Stream<T>> fn) {
        return contextsStream().flatMap((zipper) -> zipper.chainUpdateStream(fn));
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

    default <U> ImmutableList<U> chain(Function<T, ImmutableList<U>> fn) {
        ImmutableList<U> result = new Nil<>();
        ImmutableList<T> current = this;

        while (true) {
            switch (current) {
                case Nil():
                    return result.reverse();
                case Cons(T head, ImmutableList<T> tail):
                    {
                        result = result.concat(fn.apply(head).reverse());
                        current = tail;
                    }
            }
        }
    }

    default Maybe<T> getAt(int index) {
        ImmutableList<T> current = this;

        for (int i = 0; i < index; i++) {
            switch (current) {
                case Nil():
                    {
                        return Maybe.empty();
                    }
                case Cons(var head, var tail):
                    {
                        current = tail;
                    }
            }
        }

        return switch (current) {
            case Nil() -> Maybe.empty();
            case Cons(var head, var tail) -> Maybe.of(head);
        };
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
                                    result.push(value);
                                }
                        }

                        current = tail;
                    }
            }
        }
    }

    default ArrayList<T> toArrayList() {
        ImmutableList<T> reversed = this.reverse();
        ArrayList<T> result = new ArrayList<>();

        while (true) {
            switch (reversed) {
                case Nil():
                    {
                        return result;
                    }
                case Cons(T head, ImmutableList<T> tail):
                    {
                        result.add(head);
                        reversed = tail;
                    }
            }
        }
    }
}
