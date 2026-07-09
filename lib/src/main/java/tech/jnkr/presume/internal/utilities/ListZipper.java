package tech.jnkr.presume.internal.utilities;

import java.util.function.Function;
import java.util.stream.Stream;

public class ListZipper<T> {
    private final ImmutableList<T> leftSiblings;
    private final ImmutableList<T> rightSiblings;
    private final T focus;

    public T focus() {
        return focus;
    }

    public static <T> Maybe<ListZipper<T>> from(ImmutableList<T> list) {
        return switch (list) {
            case Nil() -> Maybe.empty();
            case Cons(T head, ImmutableList<T> tail) ->
                    Maybe.of(new ListZipper<>(new Nil<>(), head, tail));
        };
    }

    private ListZipper(ImmutableList<T> leftSiblings, T focus, ImmutableList<T> rightSiblings) {
        this.leftSiblings = leftSiblings;
        this.focus = focus;
        this.rightSiblings = rightSiblings;
    }

    public Maybe<ListZipper<T>> left() {
        return switch (leftSiblings) {
            case Nil() -> Maybe.empty();
            case Cons(T head, ImmutableList<T> tail) ->
                    Maybe.of(new ListZipper<>(tail, head, rightSiblings.push(focus)));
        };
    }

    public Maybe<ListZipper<T>> right() {
        return switch (rightSiblings) {
            case Nil() -> Maybe.empty();
            case Cons(T head, ImmutableList<T> tail) ->
                    Maybe.of(new ListZipper<>(leftSiblings.push(focus), head, tail));
        };
    }

    public ListZipper<T> update(Function<T, T> updater) {
        return new ListZipper<>(leftSiblings, updater.apply(focus), rightSiblings);
    }

    public ListZipper<T> replace(T newValue) {
        return new ListZipper<>(leftSiblings, newValue, rightSiblings);
    }

    public ListZipper<T> leftmost() {
        ImmutableList<T> leftReversed = leftSiblings.reverse();

        return switch (leftReversed) {
            case Nil() -> this;
            case Cons(T head, ImmutableList<T> tail) ->
                    new ListZipper<>(
                            new Nil<>(), head, rightSiblings.push(focus).concat(leftReversed));
        };
    }

    public ImmutableList<T> toList() {
        ListZipper<T> reset = leftmost();
        return new Cons<>(reset.focus, reset.rightSiblings);
    }

    public ImmutableList<ImmutableList<T>> chainUpdate(Function<T, ImmutableList<T>> fn) {
        return fn.apply(focus).map((T focus) -> this.update((T oldFocus) -> focus).toList());
    }

    public Stream<ImmutableList<T>> chainUpdateStream(Function<T, Stream<T>> fn) {
        return fn.apply(focus).map((T focus) -> this.update((T oldFocus) -> focus).toList());
    }

    public ImmutableList<T> leftSublist() {
        return leftSiblings.reverse();
    }
}
