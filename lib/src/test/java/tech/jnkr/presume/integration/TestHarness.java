package tech.jnkr.presume.integration;

import static org.junit.jupiter.api.Assertions.*;

import static tech.jnkr.presume.PropertyRunner.runProperty;
import static tech.jnkr.presume.PropertyRunner.runSlug;

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

    @Test
    void showThatSlugWorks() {
        CounterexampleException exception =
                assertThrows(
                        CounterexampleException.class,
                        () ->
                                runSlug(
                                        new IntListGenerator(),
                                        (list) -> {
                                            if (!isOrdered(badSorter(list)))
                                                throw new RuntimeException("oh no");
                                        },
                                        "rO0ABXNyAC10ZWNoLmpua3IucHJlc3VtZS5pbnRlcm5hbC51dGlsaXRpZXMuUm9zZVRyZWWp04CP0bP/4AIAAkwACGNoaWxkcmVudAA0THRlY2gvam5rci9wcmVzdW1lL2ludGVybmFsL3V0aWxpdGllcy9JbW11dGFibGVMaXN0O0wABXZhbHVldAASTGphdmEvbGFuZy9PYmplY3Q7eHBzcgApdGVjaC5qbmtyLnByZXN1bWUuaW50ZXJuYWwudXRpbGl0aWVzLkNvbnMAAAAAAAAAAAIAAkwABGhlYWRxAH4AAkwABHRhaWxxAH4AAXhwc3EAfgAAc3IAKHRlY2guam5rci5wcmVzdW1lLmludGVybmFsLnV0aWxpdGllcy5OaWwAAAAAAAAAAAIAAHhwc3EAfgAEc3IAKHRlY2guam5rci5wcmVzdW1lLmludGVybmFsLmF0b21zLlJlZ3VsYXIAAAAAAAAAAAIAA0kACW1hZ25pdHVkZVoABHNpZ25aAAhzaW1wbGlmeXhwMzNLwAEBc3EAfgAEc3IAKXRlY2guam5rci5wcmVzdW1lLmludGVybmFsLmF0b21zLlRyaXZpYWwxAAAAAAAAAAACAAB4cHNxAH4ABHNxAH4ACgAAAAYBAXNxAH4ABHNxAH4ACgAAAAEBAXNxAH4ABHNxAH4ADXNxAH4AB3NxAH4AB3NxAH4ABw=="));

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
