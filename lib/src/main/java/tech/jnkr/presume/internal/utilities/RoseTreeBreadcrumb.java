package tech.jnkr.presume.internal.utilities;

import java.util.Objects;

public record RoseTreeBreadcrumb<T>(
        T value,
        ImmutableList<RoseTree<T>> leftChildren,
        ImmutableList<RoseTree<T>> rightChildren) {

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other
                instanceof
                RoseTreeBreadcrumb<?>(
                        var otherValue,
                        ImmutableList<?> otherLeftChildren,
                        ImmutableList<?> otherRightChildren))) return false;

        return value.equals(otherValue)
                && leftChildren.equals(otherLeftChildren)
                && rightChildren.equals(otherRightChildren);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, leftChildren.hashCode(), rightChildren.hashCode());
    }
}
