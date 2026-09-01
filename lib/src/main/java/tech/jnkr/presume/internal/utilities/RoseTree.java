package tech.jnkr.presume.internal.utilities;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class RoseTree<T> implements Serializable {
    public final T value;
    public final ImmutableList<RoseTree<T>> children;

    private static final long serialVersionUID = -6209478106026868768L;

    public RoseTree(T value, ImmutableList<RoseTree<T>> children) {
        this.value = value;
        this.children = children;
    }

    public <U> RoseTree<U> map(Function<T, U> fn) {
        // TODO: stack safety
        return new RoseTree<>(fn.apply(value), children.map(tree -> tree.map(fn)));
    }

    public <U> U foldDepthFirst(BiFunction<U, T, U> fn, U initial) {
        U acc = initial;
        Stack<RoseTree<T>> stack = new Stack<>();
        stack.add(this);

        while (!stack.isEmpty()) {
            RoseTree<T> current = stack.pop();
            acc = fn.apply(acc, current.value);

            current.children.reverse().forEach(stack::add);
        }

        return acc;
    }

    public RoseTreeZipper<T> toZipper() {
        return RoseTreeZipper.fromTree(this);
    }

    public ImmutableList<RoseTree<T>> getSubtrees(int depth) {
        return getSubtreesRecursive(depth, this, ImmutableList.empty());
    }

    private static <T> ImmutableList<RoseTree<T>> getSubtreesRecursive(
            int depth, RoseTree<T> current, ImmutableList<RoseTree<T>> accumulator) {
        if (depth == 0) return accumulator;

        for (var child : current.children) {
            accumulator = accumulator.push(child);
        }

        return current.children.foldLeft(
                (acc, childTree) -> getSubtreesRecursive(depth - 1, childTree, acc), accumulator);
    }

    public ImmutableList<RoseTreeZipper<T>> getContexts() {
        ImmutableList<RoseTreeZipper<T>> result = ImmutableList.empty();
        ImmutableList<RoseTreeZipper<T>> stack = ImmutableList.of(toZipper());

        while (true) {
            switch (stack) {
                case Nil():
                    return result.reverse();
                case Cons(var current, var tail):
                    {
                        stack = tail;
                        result = result.push(current);

                        for (int i = 0; i < current.childCount(); i++) {
                            stack = stack.push(current.nthChild(i).unwrapUnsafe());
                        }
                    }
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        // TODO replace this with safeEquals below once I can test it
        if (this == other) return true;

        if (!(other instanceof RoseTree<?> otherTree)) return false;

        if (!value.equals(otherTree.value)) return false;

        return children.equals(otherTree.children);
    }

    @Override
    public int hashCode() {
        // TODO replace this with safeHashCode below once I can test it
        return Objects.hash(value, children.hashCode());
    }

    public boolean safeEquals(Object other) {
        if (this == other) return true;
        if (!(other instanceof RoseTree<?> otherTree)) return false;

        Queue<Tuple<Object, Object>> queue = new ArrayDeque<>();
        queue.add(new Tuple<>(this, otherTree));

        while (!queue.isEmpty()) {
            var pair = queue.remove();
            Object first = pair.first();
            Object second = pair.second();

            if (first == second) continue;

            if (!(first instanceof RoseTree<?> firstTree)
                    || !(second instanceof RoseTree<?> secondTree)) return false;

            if (!firstTree.value.equals(secondTree.value)) return false;

            ImmutableList<? extends Object> firstChildren = firstTree.children;
            ImmutableList<? extends Object> secondChildren = secondTree.children;

            while (firstChildren instanceof Cons<? extends Object> firstCons) {
                if (secondChildren instanceof Cons<? extends Object> secondCons) {
                    queue.add(new Tuple<>(firstCons.head(), secondCons.head()));
                    firstChildren = firstCons.tail();
                    secondChildren = secondCons.tail();
                } else {
                    return false;
                }
            }

            if (secondChildren instanceof Cons<?>) return false;
        }

        return true;
    }

    public int safeHashCode() {
        return foldDepthFirst(Objects::hash, 1);
    }

    @Override
    public String toString() {
        return "RoseTree{" + "value=" + value + ", children=" + children + '}';
    }
}
