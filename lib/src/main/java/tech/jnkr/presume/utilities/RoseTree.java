package tech.jnkr.presume.utilities;

import java.util.List;

public record RoseTree<T>(T value, List<RoseTree<T>> children) {}
