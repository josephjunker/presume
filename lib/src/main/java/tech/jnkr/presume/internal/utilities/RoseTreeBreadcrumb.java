package tech.jnkr.presume.internal.utilities;

public record RoseTreeBreadcrumb<T>(
        T value,
        ImmutableList<RoseTree<T>> leftChildren,
        ImmutableList<RoseTree<T>> rightChildren) {}
