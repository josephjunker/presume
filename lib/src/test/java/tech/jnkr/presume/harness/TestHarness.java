package tech.jnkr.presume.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static tech.jnkr.presume.PropertyRunner.runProperty;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import tech.jnkr.presume.ExampleGenerator;
import tech.jnkr.presume.ExampleIntListGenerator;

import java.util.ArrayList;
import java.util.List;

@Disabled
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
                10000);
    }

    private List<Integer> badSorter(List<Integer> list) {
        List<Integer> result = new ArrayList<>(list);
        result.sort(null);

        if (list.size() > 3
                && (list.get(0) % 2 == 0)
                && (list.get(2) % 2 == 1)
                && (list.get(1) > 3)
                && (list.get(1) % 3 == 0)) {
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

    @Test
    public void assertThatBuggyEval1Works() {
        runProperty(
                new ExprGenerator(),
                (expr) -> {
                    assertEquals(ExprOperations.eval(expr), ExprOperations.buggyEval1(expr));
                },
                1000);
    }
}
