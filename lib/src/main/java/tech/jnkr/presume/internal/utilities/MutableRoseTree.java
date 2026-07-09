package tech.jnkr.presume.internal.utilities;

import java.util.ArrayList;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MutableRoseTree<T> {
    public T value;
    public ArrayList<MutableRoseTree<T>> children;

    public MutableRoseTree(T value) {
        this.value = value;
        this.children = new ArrayList<>();
    }

    public <U> MutableRoseTree<U> map(Function<T, U> fn) {
        MutableRoseTree<U> result = new MutableRoseTree<>(fn.apply(value));

        Stack<Tuple<MutableRoseTree<T>, MutableRoseTree<U>>> stack = new Stack<>();

        stack.push(new Tuple<>(this, result));

        while (!stack.isEmpty()) {
            Tuple<MutableRoseTree<T>, MutableRoseTree<U>> item = stack.pop();

            for (MutableRoseTree<T> child : item.first().children) {
                U value = fn.apply(child.value);
                MutableRoseTree<U> mappedChild = new MutableRoseTree<>(value);
                item.second().children.add(mappedChild);
                stack.push(new Tuple<>(child, mappedChild));
            }
        }

        return result;
    }

    public RoseTree<T> toImmutable() {
        return new RoseTree<>(
                value,
                ImmutableList.fromList(
                        children.stream()
                                .map(MutableRoseTree::toImmutable)
                                .collect(Collectors.toList())));
    }
}
