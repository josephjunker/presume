package tech.jnkr.presume.internal.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

public class ImmutableListTests {
    static Stream<Arguments> provideLists() {
        ImmutableList<Integer> list1 =
                new Cons<>(
                        1, new Cons<>(2, new Cons<>(3, new Cons<>(4, new Cons<>(5, new Nil<>())))));

        ImmutableList<Integer> list2 = new Nil<>();

        ImmutableList<Integer> list3 = new Cons<>(1, new Nil<>());

        return Stream.of(Arguments.of(list1), Arguments.of(list2), Arguments.of(list3));
    }

    @DisplayName("fromList() and toArrayList() methods round-trip")
    @ParameterizedTest()
    @MethodSource("provideLists")
    void roundTripsWithArrayList(ImmutableList<Integer> list) {
        assertEquals(list, ImmutableList.fromList(list.toArrayList()));
    }

    @DisplayName("push() and pop() are inverses")
    @ParameterizedTest()
    @MethodSource("provideLists")
    void pushPopInverse(ImmutableList<Integer> list) {
        var newList = list.push(100);
        switch (newList) {
            case Nil():
                throw new RuntimeException("Failed");
            case Cons(var head, var tail):
                assertEquals(100, head);
        }

        assertEquals(list, newList.pop());
    }

    @DisplayName("reverse() is its own inverse")
    @ParameterizedTest()
    @MethodSource("provideLists")
    void reverseRoundTrip(ImmutableList<Integer> list) {
        assertEquals(list, list.reverse().reverse());
    }

    @DisplayName("reverse() example")
    @Test
    void basicReverse() {
        assertEquals(
                ImmutableList.fromList(List.of(1, 2, 3)),
                ImmutableList.fromList(List.of(3, 2, 1)).reverse());
    }

    @DisplayName("concat() left identity")
    @ParameterizedTest()
    @MethodSource("provideLists")
    void concatLeftIdentity(ImmutableList<Integer> list) {
        assertEquals(list, new Nil<Integer>().concat(list));
    }

    @DisplayName("concat() right identity")
    @ParameterizedTest()
    @MethodSource("provideLists")
    void concatRightIdentity(ImmutableList<Integer> list) {
        assertEquals(list, list.concat(new Nil<>()));
    }

    @DisplayName("concat() example based test")
    @Test
    void concatExample() {
        ImmutableList<Integer> prefix = ImmutableList.fromList(List.of(1, 2, 3));
        ImmutableList<Integer> suffix = ImmutableList.fromList(List.of(4, 5));

        assertEquals(prefix.concat(suffix), ImmutableList.fromList(List.of(1, 2, 3, 4, 5)));
    }

    @DisplayName("map() with identity function is identity")
    @ParameterizedTest()
    @MethodSource("provideLists")
    void mapIdentity(ImmutableList<Integer> list) {
        assertEquals(list, list.map(x -> x));
    }

    @DisplayName("map() with inverse is identity")
    @ParameterizedTest()
    @MethodSource("provideLists")
    void mapInverseIdentity(ImmutableList<Integer> list) {
        assertEquals(list, list.map(x -> x - 1).map(x -> x + 1));
    }

    @DisplayName("map() example based test")
    @Test
    void mapExample() {
        ImmutableList<Integer> input = ImmutableList.fromList(List.of(1, 2, 3));
        ImmutableList<Integer> output = ImmutableList.fromList(List.of(2, 4, 6));
        assertEquals(output, input.map(x -> x * 2));
    }

    @DisplayName("filterMap() with Just is identity")
    @ParameterizedTest()
    @MethodSource("provideLists")
    void filterMapJustIdentity(ImmutableList<Integer> list) {
        assertEquals(list, list.filterMap(Maybe::of));
    }

    @DisplayName("filterMap() example based test")
    @Test
    void filterMapExample() {
        ImmutableList<Integer> input = ImmutableList.fromList(List.of(1, 2, 3, 4));
        ImmutableList<Integer> output = ImmutableList.fromList(List.of(1, 3));
        assertEquals(output, input.filterMap(x -> x % 2 == 0 ? Maybe.empty() : Maybe.of(x)));
    }

    @DisplayName("foldLeft() with push() is reverse()")
    @ParameterizedTest()
    @MethodSource("provideLists")
    void foldLeftPushReverse(ImmutableList<Integer> list) {
        assertEquals(
                list.foldLeft(
                        (ImmutableList<Integer> l, Integer i) -> l.push(i), ImmutableList.empty()),
                list.reverse());
    }
}
