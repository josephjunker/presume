package tech.jnkr.presume.internal.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiFunction;

public class RoseTreeTests {

    @Test
    @DisplayName(".foldDepthFirst collects lists (base case)")
    void foldDepthFirstSmokeTest1() {
        BiFunction<ImmutableList<Integer>, Integer, ImmutableList<Integer>> collect =
                ImmutableList::push;

        assertEquals(
                new RoseTree<>(1, ImmutableList.empty())
                        .foldDepthFirst(collect, ImmutableList.empty()),
                ImmutableList.of(1));
    }

    @Test
    @DisplayName(".foldDepthFirst collects lists (big tree)")
    void foldDepthFirstSmokeTest2() {
        BiFunction<ImmutableList<Integer>, Integer, ImmutableList<Integer>> collect =
                ImmutableList::push;

        RoseTree<Integer> tree =
                new RoseTree<>(
                        1,
                        ImmutableList.fromList(
                                List.of(
                                        new RoseTree<>(
                                                2,
                                                ImmutableList.fromList(
                                                        List.of(
                                                                new RoseTree<>(
                                                                        3,
                                                                        ImmutableList.empty())))),
                                        new RoseTree<>(
                                                4,
                                                ImmutableList.fromList(
                                                        List.of(
                                                                new RoseTree<>(
                                                                        5, ImmutableList.empty()),
                                                                new RoseTree<>(
                                                                        6,
                                                                        ImmutableList.fromList(
                                                                                List.of(
                                                                                        new RoseTree<>(
                                                                                                7,
                                                                                                ImmutableList
                                                                                                        .empty()))))))))));

        assertEquals(
                ImmutableList.fromList(List.of(7, 6, 5, 4, 3, 2, 1)),
                tree.foldDepthFirst(collect, ImmutableList.empty()));
    }
}
