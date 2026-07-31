package tech.jnkr.presume.internal.utilities;

import java.util.ArrayList;
import java.util.Objects;
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

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (!(other instanceof RoseTree<?> otherTree)) return false;

        if (!value.equals(otherTree.value)) return false;

        return children.equals(otherTree.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, children.hashCode());
    }
}
