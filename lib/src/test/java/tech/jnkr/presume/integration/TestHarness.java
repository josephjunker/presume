package tech.jnkr.presume.integration;

import static org.junit.jupiter.api.Assertions.*;

import static tech.jnkr.presume.PropertyRunner.runProperty;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import tech.jnkr.presume.exceptions.CounterexampleException;

import java.util.ArrayList;
import java.util.List;

class TestHarness {
    @RepeatedTest(100)
    void assertThatAllBooleansAreTrue_findsMinimalCase() {
        CounterexampleException exception =
                assertThrows(
                        CounterexampleException.class,
                        () ->
                                runProperty(
                                        new SimpleBooleanGenerator(),
                                        (bool) -> {
                                            assertEquals(true, bool);
                                        }));

        assertEquals(false, exception.counterexample);
    }

    @Test
    void assertThatSortWorks() {
        runProperty(
                new IntListGenerator(),
                (list) -> {
                    list.sort(null);
                    assertTrue(isOrdered(list));
                });
    }

    @RepeatedTest(100)
    void assertThatBadSortWorks_findsMinimalCounterexample() {
        CounterexampleException exception =
                assertThrows(
                        CounterexampleException.class,
                        () ->
                                runProperty(
                                        new IntListGenerator(),
                                        (list) -> {
                                            if (!isOrdered(badSorter(list)))
                                                throw new RuntimeException("oh no");
                                        },
                                        10000));

        ArrayList<Integer> expectedCounterexample = new ArrayList<>(List.of(0, 6, 1, 0));

        assertEquals(expectedCounterexample, exception.counterexample);
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

    @RepeatedTest(100)
    public void assertThatBuggyEval1Works_findsMinimalCounterexample() {
        Expr minimal =
                new Expr.Mul(
                        new Expr.Add(new Expr.Lit(0), new Expr.Lit(0)),
                        new Expr.Neg(new Expr.Lit(0)));

        CounterexampleException exception =
                assertThrows(
                        CounterexampleException.class,
                        () ->
                                runProperty(
                                        new ExprGenerator(),
                                        (expr) -> {
                                            assertEquals(
                                                    ExprOperations.eval(expr),
                                                    ExprOperations.buggyEval1(expr));
                                        },
                                        10000));

        assertEquals(minimal, exception.counterexample);
    }
}
