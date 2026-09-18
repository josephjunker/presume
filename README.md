# Presume: direct-style property-based testing for Java

`presume` is a testing library loosely based on the Hypothesis library in the Python ecosystem. The main goal of the library is to enable writing succinct, powerful tests through the use of random data generators. Presume differentiates itself from other Java property-based testing libraries by using a direct-style interface, allowing complex data generators to be written imperatively without the use of nested lambdas.

An introduction to the basic ideas behind property-based testing [lives here](/docs/what-is-property-based-testing.md).

Presume is a work-in-progress. Its core functionality of data generation, shrinking, and test reproduction is stable, but it lacks some convenience functions which are present in more comprehensive libraries. This is hopefully a temporary state of affairs; as of September 2026 this library is still under active development.

## Defining tests

Running a presume test requires providing a generator for test data, and an assertion function which throws when the test should fail. For example, here is a test asserting that a sorting algorithm works when given an array of integers, written using the JUnit testing framework:

```java
@Test
void testSort1() {
    runProperty(
            new IntListGenerator(),
            (list) -> assertTrue(isOrdered(sort(list))));
}
```

The first argument is a test data generator. Here is its implementation:

```java
public class IntListGenerator extends AbstractGenerator<ArrayList<Integer>> {
    @Override
    protected ArrayList<Integer> gen(GenerationSource source) {
        int length = source.getInteger(0, 10);
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            result.add(source.getInteger());
        }

        return result;
    }
}
```

All data generators subclass the `AbstractGenerator` class, and implement a protected `gen` method. This method has the ability to pull random values from a `GenerationSource` object. In this example we are using the source to produce random integers; first a value from 0 to 9 to determine the length of the list being produced, and then an additional invocation of `getInteger()` in each pass through the loop.

This example shows what makes presume "direct-style". In most property-based testing libraries such a generator would be written something like this pseudocode:

```java
PseudocodeGenerator<ArrayList<Integer>> generator = new PseudocodeIntegerGenerator(0, 9).flatMap(
        length -> {
            ArrayList<Integer> result = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                result.add(source.getInteger());
            }

            return result;
        }
);
```

This shows a monadic approach to data generation, in which accessing the output of one generator inside of another generator requires monadic composition using a `flatMap` method. This is a reasonable approach in some programming languages, but in Java this can become hard to maintain when a large number of `flatMap` invocations need to be nested, or when the data being generated is naturally recursive. Presume supports monadic generation but it is not recommended outside of simple cases.

## Test repetitions

Here is a full implementation of the example before, using a `sort` method which contains a bug. This bug is intended to be an extreme edge case; a black-box example-based test would never catch it.

```java
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

private List<Integer> badSorter(List<Integer> list) {
    List<Integer> result = new ArrayList<>(list);
    result.sort(null);

    if (list.size() > 3
            && (list.get(0) % 2 == 0)
            && (list.get(1) > 3)
            && (list.get(1) % 3 == 0)
            && (list.get(2) % 2 == 1)) {
        result.sort(null);
        return result.reversed();
    }

    return result;
}
```

`isOrdered` is our assertion method. We say that if a list is sorted, then its contents should be in order after the sort is complete. Our sorting method just calls the `ArrayList` sort method, except in a very specific case. If:
- The list being sorted has at least 3 elements
- The first item is even
- The second item is a multiple of 3, greater than 3
- The third item is odd
then this method will return an incorrect result. Let's run it:

```java
@Test
void testSort2() {
    runProperty(
            new IntListGenerator(),
            (list) -> assertTrue(isOrdered(badSorter(list))));
}
```

If you do this locally, the test will probably pass. Uh-oh! This is because the edge case being reproduced is very, very specific. By default presume will run each test 100 times, which is sufficient for finding simple edge cases but not for reliably finding extremely rare ones. To make this example work, we need to manually specify the number of test repetitions to attempt:

```java
@Test
void testSort3() {
    runProperty(
            new IntListGenerator(),
            (list) -> assertTrue(isOrdered(badSorter(list))),
            10_000);
}
```

Now this test will run ten thousand times, and for this specific example, will almost certainly fail.

## Reading failures

When the test fails, you'll see something like this printed to the console:

```
Property failed! Shrunk 36 times.

Seed:
rO0ABXNyAC10ZWNoLmpua3IucHJlc3VtZS5pbnRlcm5hbC51dGlsaXRpZXMuUm9zZVRyZWWp04CP0bP/4AIAAkwACGNoaWxkcmVudAA0THRlY2gvam5rci9wcmVzdW1lL2ludGVybmFsL3V0aWxpdGllcy9JbW11dGFibGVMaXN0O0wABXZhbHVldAASTGphdmEvbGFuZy9PYmplY3Q7eHBzcgApdGVjaC5qbmtyLnByZXN1bWUuaW50ZXJuYWwudXRpbGl0aWVzLkNvbnMAAAAAAAAAAAIAAkwABGhlYWRxAH4AAkwABHRhaWxxAH4AAXhwc3EAfgAAc3IAKHRlY2guam5rci5wcmVzdW1lLmludGVybmFsLnV0aWxpdGllcy5OaWwAAAAAAAAAAAIAAHhwc3EAfgAEc3IAKHRlY2guam5rci5wcmVzdW1lLmludGVybmFsLmF0b21zLlJlZ3VsYXIAAAAAAAAAAAIAA0kACW1hZ25pdHVkZVoABHNpZ25aAAhzaW1wbGlmeXhwMzNLwAEBc3EAfgAEc3IAKXRlY2guam5rci5wcmVzdW1lLmludGVybmFsLmF0b21zLlRyaXZpYWwxAAAAAAAAAAACAAB4cHNxAH4ABHNxAH4ACgAAAAYBAXNxAH4ABHNxAH4ACgAAAAEBAXNxAH4ABHNxAH4ADXNxAH4AB3NxAH4AB3NxAH4ABw==

Minimal counterexample:
[0, 6, 1, 0]

Inner exception:
org.opentest4j.AssertionFailedError: expected: <true> but was: <false>
```

Note that the minimal counterexample, `[0, 6, 1, 0]` is the smallest possible input that could trigger our edge case. Presume was able to find it without being aware of our function's implementation at all. It was found by generating larger lists with arbitrarily large integer values until an error was detected, and then progressively reducing the size of the list and its contents until no smaller counterexamples could be found.

## Determinism and debugging

Presume's test data generation is intentionally nondeterministic. This means that a test which passes one time may fail another time, if the first invocation didn't perform enough attempts to reproduce the error. The test seed shown in the test output above is how presume maintains a reasonable developer experience in the face of nondeterminism.

If you see a failure, locally or in a CI build, you can reproduce and debug it by using `runSlug` instead of `runProperty`:

```java
@Test
void testSort4() {
    runSlug(
            new IntListGenerator(),
            (list) -> assertTrue(isOrdered(badSorter(list))),
            "rO0FULL_SEED_GOES_HERE_AH4ABw==");
}
```

The third argument to `runSlug` is the "seed" value printed in a test failure. This seed encodes the failing test data, **after** shrinking. Your test types do not need to implement serialization methods; so long as your test generators themselves are deterministic presume can shrink and replay the test data. This is essential when using a debugger; it's not tenable to step through hundreds of runs during the search for a counterexample, but `runSlug` will only run the test once.

**IMPORTANT NOTE**: The format of the seed/slug values is not stable between releases of presume, and will likely change in the future. One reasonable workflow is to find counterexamples using property-based tests, and then add them to the test suite as example-based tests to get extra deterministic guarantees against specific regressions. `runSlug` is the **wrong** tool for this, because a new library release may make existing slugs unusuable. Slugs are a debugging tool, and should not be checked in to source control.

## Edge cases

Presume will proactively produce edge cases for primitive values. For example, and integer will be 1, 0, or the minimum or maximum value of its range some percentage of the time. Floating point numbers have around a 1% chance each of being NaN, infinity, or negative infinity. If you do not want to deal with these edge cases you can use `GenerationSource::getBoringFloat` instead of `GenerationSource::getFloat`, or you can use `GenerationSource::floatGen` to get a specific float generator, whose edge cases can be enabled or disabled individually using `allow` and `disallow` methods.

## Composing generators

Note that the `gen` method of generators is `protected`. One generator should not call the `gen` method of another generator; doing so would harm shrinking behavior. Rather, generators should use
the `source.call` method of their `GenerationSource` to invoke each other:

```java
record Pair(boolean first, int second);

class SampleGenerator1 extends AbstractGenerator<Pair> {
    @Override
    protected Pair gen(@NonNull GenerationSource source) {
        boolean b = source.getBoolean();
        int i = source.call(new SampleGenerator2()); // This is the important part!
    }
}

class SampleGenerator2 extends AbstractGenerator<Integer> {
    @Override
    protected Integer gen(@NonNull GenerationSource source) {
        return source.getInteger();
    }
}
```

## Transforming generators

Generators provide `map`, `flatMap`, and `filter` methods. For instance, say we wanted to make a generator which only produces even integers. We could do so using `filter`:

```java
class SampleGenerator3 extends AbstractGenerator<Integer> {
    @Override
    protected Integer gen(@NonNull GenerationSource source) {
        AbstractGenerator<Integer> evens = source.integerGen.filter(x -> x % 2 == 0);
        return source.call(evens);
    }
}
```

`filter` is often an easy way to enforce _preconditions_ on our test inputs, defining the domain of values which our asserted test behavior applies to. However, overuse of filtering can hurt performance. In this example our generator will produce twice as many values as are actually needed, slowing down test data generation. How much of a problem this is depends on the size of your test suite, the complexity of your generators, and your repetition count.

As an alternative to `filter`, we can use `map` to make values which pass our preconditions _by construction_. If we want to only deal with even numbers, we can just take arbitrary integer values and double them (being careful to avoid integer overflow!) Here's one way to do that:

```java
class SampleGenerator4 extends AbstractGenerator<Integer> {
    @Override
    protected Integer gen(@NonNull GenerationSource source) {
        AbstractGenerator<Integer> evens = source.integerGen
                .withMinimum(-10_000_000)
                .withMaximum(10_000_000)
                .map(x -> x * 2);
        
        return source.call(evens);
    }
}
```

## License

MIT

## Contributing

Please read CONTRIBUTING.md before opening issues, discussions, or pull requests on this repository.

## Citations

This project uses a distinct integrated generation and shrinking algorithm from Hypothesis, but was still heavily influenced by the following paper:

> MacIver, David R., and Alastair F. Donaldson. "Test-case reduction via test-case generation: Insights from the hypothesis reducer (tool insights paper)." 34th European Conference on Object-Oriented Programming (ECOOP 2020). Schloss Dagstuhl-Leibniz-Zentrum für Informatik, 2020.