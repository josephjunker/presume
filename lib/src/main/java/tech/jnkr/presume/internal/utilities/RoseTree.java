package tech.jnkr.presume.internal.utilities;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;

public class RoseTree<T> {
    public final T value;
    public final ImmutableList<RoseTree<T>> children;

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
        ArrayList<RoseTree<T>> queue = new ArrayList<>();
        queue.add(this);
        int head = 0;

        while (head < queue.size()) {
            RoseTree<T> current = queue.get(head);
            head++;
            acc = fn.apply(acc, current.value);

            current.children.forEach(queue::add);
        }

        return acc;
    }

    public RoseTreeZipper<T> toZipper() {
        return RoseTreeZipper.fromTree(this);
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
}
