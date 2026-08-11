package tech.jnkr.presume.internal.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class ListZipperTests {
    static Stream<Arguments> provideLists() {
        ImmutableList<Integer> list1 = new Nil<>();
        ImmutableList<Integer> list2 = new Cons<>(5, list1);
        ImmutableList<Integer> list3 = new Cons<>(4, list2);
        ImmutableList<Integer> list4 = new Cons<>(3, list3);
        ImmutableList<Integer> list5 = new Cons<>(2, list4);
        ImmutableList<Integer> list6 = new Cons<>(1, list5);

        return Stream.of(
                Arguments.of(list1),
                Arguments.of(list2),
                Arguments.of(list3),
                Arguments.of(list4),
                Arguments.of(list5),
                Arguments.of(list6));
    }

    @DisplayName("toZipper() and toList() round-trip for non-empty lists")
    @ParameterizedTest()
    @MethodSource("provideLists")
    void zipperListRoundTrip(ImmutableList<Integer> list) {
        switch (list) {
            case Nil():
                {
                    assertEquals(list.toZipper(), Maybe.empty());
                    break;
                }
            case Cons(var head, var tail):
                {
                    assertEquals(list.toZipper().map(ListZipper::toList), Maybe.of(list));
                }
        }
    }

    @DisplayName("right() and left() round-trip sufficiently-long lists")
    @ParameterizedTest()
    @MethodSource("provideLists")
    void rightLeftRoundTrip(@NonNull ImmutableList<Integer> list) {
        var maybeZipper = list.toZipper();
        if (list.size() > 1) {
            assertEquals(maybeZipper, maybeZipper.chain(ListZipper::right).chain(ListZipper::left));
        } else {
            assertEquals(
                    Maybe.empty(), maybeZipper.chain(ListZipper::right).chain(ListZipper::left));
        }
    }

    @DisplayName("toZipper().right() round trips with toList() for sufficiently long lists")
    @ParameterizedTest()
    @MethodSource("provideLists")
    void toZipperRightToList(@NonNull ImmutableList<Integer> list) {
        var maybeZipper = list.toZipper();
        for (int i = 1; i < list.size(); i++) {
            maybeZipper = maybeZipper.chain(ListZipper::right);
            assertEquals(maybeZipper.map(ListZipper::toList), Maybe.of(list));
        }
    }

    @DisplayName("replace() replaces the current focus")
    @ParameterizedTest()
    @MethodSource("provideLists")
    void replaceWorksAtLeftmost(@NonNull ImmutableList<Integer> list) {
        var maybeZipper = list.toZipper();
        if (maybeZipper instanceof Nothing<ListZipper<Integer>>) return;
        var zipper = maybeZipper.unwrapUnsafe();

        var newZipper = zipper.replace(42);

        assertEquals(42, newZipper.focus());
        var newTail = newZipper.toList().pop();
        var originalTail = zipper.toList().pop();

        assertEquals(newTail, originalTail);
    }
}
