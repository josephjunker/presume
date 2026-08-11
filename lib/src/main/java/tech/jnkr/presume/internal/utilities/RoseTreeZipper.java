package tech.jnkr.presume.internal.utilities;

import java.util.Objects;
import java.util.function.Function;

public class RoseTreeZipper<T> {
    private final ImmutableList<RoseTreeBreadcrumb<T>> parents;
    private final RoseTree<T> focus;
    private final ImmutableList<RoseTree<T>> leftSiblings;
    private final ImmutableList<RoseTree<T>> rightSiblings;

    private RoseTreeZipper(
            ImmutableList<RoseTreeBreadcrumb<T>> parents,
            ImmutableList<RoseTree<T>> leftSiblings,
            RoseTree<T> focus,
            ImmutableList<RoseTree<T>> rightSiblings) {
        this.parents = parents;
        this.leftSiblings = leftSiblings;
        this.focus = focus;
        this.rightSiblings = rightSiblings;
    }

    public static <T> RoseTreeZipper<T> fromTree(RoseTree<T> tree) {
        return new RoseTreeZipper<>(
                ImmutableList.empty(), ImmutableList.empty(), tree, ImmutableList.empty());
    }

    public T value() {
        return focus.value;
    }

    public Maybe<RoseTreeZipper<T>> right() {
        return switch (rightSiblings) {
            case Nil() -> Maybe.empty();
            case Cons(RoseTree<T> head, ImmutableList<RoseTree<T>> tail) ->
                    Maybe.of(new RoseTreeZipper<>(parents, leftSiblings.push(focus), head, tail));
        };
    }

    public Maybe<RoseTreeZipper<T>> leftmostChild() {
        return switch (focus.children) {
            case Nil() -> Maybe.empty();
            case Cons(RoseTree<T> head, ImmutableList<RoseTree<T>> tail) ->
                    Maybe.of(
                            new RoseTreeZipper<>(
                                    parents.push(
                                            new RoseTreeBreadcrumb<>(
                                                    focus.value, leftSiblings, rightSiblings)),
                                    ImmutableList.empty(),
                                    head,
                                    tail));
        };
    }

    public Maybe<RoseTreeZipper<T>> nthChild(int index) {
        var maybeZipper = leftmostChild();
        int i = 0;
        while (i < index) {
            maybeZipper = maybeZipper.chain(RoseTreeZipper::right);
            i++;
        }

        return maybeZipper;
    }

    public Maybe<RoseTreeZipper<T>> up() {
        return switch (parents) {
            case Nil():
                {
                    yield Maybe.empty();
                }
            case Cons(
                    RoseTreeBreadcrumb(
                            T value,
                            ImmutableList<RoseTree<T>> aunts,
                            ImmutableList<RoseTree<T>> uncles),
                    ImmutableList<RoseTreeBreadcrumb<T>> ancestors):
                {
                    ImmutableList<RoseTree<T>> newRightSiblings = rightSiblings;
                    ImmutableList<RoseTree<T>> cursor = leftSiblings;

                    newRightSiblings = rightSiblings.push(focus);

                    while (cursor instanceof Cons) {
                        newRightSiblings =
                                newRightSiblings.push(((Cons<RoseTree<T>>) cursor).head());
                        cursor = ((Cons<RoseTree<T>>) cursor).tail();
                    }

                    yield Maybe.of(
                            new RoseTreeZipper<>(
                                    ancestors,
                                    aunts,
                                    new RoseTree<>(value, newRightSiblings),
                                    uncles));
                }
        };
    }

    public RoseTreeZipper<T> update(Function<T, T> fn) {
        return new RoseTreeZipper<>(
                parents,
                leftSiblings,
                new RoseTree<>(fn.apply(focus.value), focus.children),
                rightSiblings);
    }

    public RoseTreeZipper<T> replace(T newValue) {
        return new RoseTreeZipper<>(
                parents, leftSiblings, new RoseTree<>(newValue, focus.children), rightSiblings);
    }

    public RoseTree<T> toTree() {
        RoseTreeZipper<T> last = this;
        Maybe<RoseTreeZipper<T>> current = this.up();

        while (current.isJust()) {
            last = current.unwrapUnsafe();
            current = current.chain(RoseTreeZipper::up);
        }

        return last.focus;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof RoseTreeZipper<?> otherZipper)) return false;
        return parents.equals(otherZipper.parents)
                && focus.equals(otherZipper.focus)
                && leftSiblings.equals(otherZipper.leftSiblings)
                && rightSiblings.equals(otherZipper.rightSiblings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                parents.hashCode(),
                focus.hashCode(),
                leftSiblings.hashCode(),
                rightSiblings.hashCode());
    }
}
