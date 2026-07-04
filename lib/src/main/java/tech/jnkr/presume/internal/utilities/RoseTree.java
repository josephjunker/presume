package tech.jnkr.presume.internal.utilities;

public class RoseTree<T> {
    public final T value;
    public final ImmutableList<RoseTree<T>> children;

    public RoseTree(T value, ImmutableList<RoseTree<T>> children) {
        this.value = value;
        this.children = children;
    }
}
