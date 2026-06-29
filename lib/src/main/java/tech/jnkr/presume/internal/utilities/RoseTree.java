package tech.jnkr.presume.internal.utilities;

import java.util.List;

public record RoseTree<T>(T value, List<RoseTree<T>> children) {}
