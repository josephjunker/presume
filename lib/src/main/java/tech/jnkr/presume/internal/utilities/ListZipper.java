package tech.jnkr.presume.internal.utilities;

import java.util.Objects;

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

    public ListZipper<T> replace(T newValue) {
        return new ListZipper<>(leftSiblings, newValue, rightSiblings);
    }

    public ListZipper<T> leftmost() {
        ImmutableList<T> newRightSiblings = rightSiblings;
        T newFocus = focus;
        ImmutableList<T> currentLeftSiblings = leftSiblings;

        while (true) {
            switch (currentLeftSiblings) {
                case Nil():
                    return new ListZipper<>(new Nil<>(), newFocus, newRightSiblings);
                case Cons(T head, ImmutableList<T> tail):
                    {
                        newRightSiblings = newRightSiblings.push(newFocus);
                        newFocus = head;
                        currentLeftSiblings = tail;
                    }
            }
        }
    }

    public ImmutableList<T> toList() {
        ListZipper<T> reset = leftmost();
        return new Cons<>(reset.focus, reset.rightSiblings);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ListZipper<?> otherZipper)) return false;
        return focus.equals(otherZipper.focus)
                && leftSiblings.equals(otherZipper.leftSiblings)
                && rightSiblings.equals(otherZipper.rightSiblings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(focus, leftSiblings, rightSiblings);
    }
}
