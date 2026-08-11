package tech.jnkr.presume.internal.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

public class RoseTreeZipperTests {

    @Test
    @DisplayName("toTree round-trips with toZipper")
    public void toZipperToTree() {
        assertEquals(getBigTree(), getBigTree().toZipper().toTree());
        assertEquals(getSmallTree(), getSmallTree().toZipper().toTree());
    }

    @Test
    @DisplayName("basic navigation works")
    public void navigationTest1() {
        RoseTreeZipper<Integer> zipper = getBigTree().toZipper();

        var moved1 = zipper.leftmostChild().chain(RoseTreeZipper::right);

        assertEquals(moved1.map(RoseTreeZipper::value), Maybe.of(4));

        var moved2 = moved1.chain(RoseTreeZipper::leftmostChild);

        assertEquals(moved2.map(RoseTreeZipper::value), Maybe.of(5));

        assertEquals(moved2.chain(RoseTreeZipper::up), moved1);
        assertEquals(moved1.map(RoseTreeZipper::toTree), moved2.map(RoseTreeZipper::toTree));
    }

    @Test
    @DisplayName("nthChild works")
    public void navigationTest2() {
        assertEquals(Maybe.empty(), getSmallTree().toZipper().nthChild(0));

        var zipper = getBigTree().toZipper();
        assertEquals(zipper.leftmostChild(), zipper.nthChild(0));
        assertEquals(zipper.leftmostChild().chain(RoseTreeZipper::right), zipper.nthChild(1));

        assertEquals(
                zipper.nthChild(1)
                        .chain(z -> z.nthChild(1))
                        .chain(z -> z.nthChild(0))
                        .map(RoseTreeZipper::value),
                Maybe.of(7));
    }

    public RoseTree<Integer> getSmallTree() {
        return new RoseTree<>(0, ImmutableList.empty());
    }

    public RoseTree<Integer> getBigTree() {
        return new RoseTree<>(
                1,
                ImmutableList.fromList(
                        List.of(
                                new RoseTree<>(
                                        2,
                                        ImmutableList.fromList(
                                                List.of(new RoseTree<>(3, ImmutableList.empty())))),
                                new RoseTree<>(
                                        4,
                                        ImmutableList.fromList(
                                                List.of(
                                                        new RoseTree<>(5, ImmutableList.empty()),
                                                        new RoseTree<>(
                                                                6,
                                                                ImmutableList.fromList(
                                                                        List.of(
                                                                                new RoseTree<>(
                                                                                        7,
                                                                                        ImmutableList
                                                                                                .empty()))))))))));
    }
}
