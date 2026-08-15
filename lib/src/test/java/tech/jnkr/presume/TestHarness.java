package tech.jnkr.presume;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static tech.jnkr.presume.PropertyRunner.runProperty;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class TestHarness {
    @Test
    void assertThatAllBooleansAreTrue() {

        runProperty(
                new ExampleGenerator(),
                (bool) -> {
                    // Assert that all booleans are true
                    // if (!bool) throw new RuntimeException("Was false");
                    assertEquals(true, bool);
                });
    }

    @Test
    void assertThatSortWorks() {
        runProperty(
                new ExampleIntListGenerator(),
                (list) -> {
                    list.sort(null);
                    assertTrue(isOrdered(list));
                });
    }

    @Test
    void assertThatBadSortWorks() {
        runProperty(
                new ExampleIntListGenerator(),
                (list) -> {
                    if (!isOrdered(badSorter(list))) throw new RuntimeException("oh no");
                },
                1000);
    }

    private List<Integer> badSorter(List<Integer> list) {
        List<Integer> result = new ArrayList<>(list);
        result.sort(null);

        if (list.size() > 3 && (list.get(0) % 2 == 0) && (list.get(2) % 2 == 1)) {
            result.sort(null);
            return result.reversed();
        }

        return result;
    }

    private boolean isOrdered(List<Integer> list) {
        if (list.isEmpty()) return true;

        int lastValue = list.getFirst();
        for (int i = 1; i < list.size(); i++) {
            int currentValue = list.get(i);
            if (lastValue > currentValue) return false;
            lastValue = currentValue;
        }

        return true;
    }
}
