# What is property-based testing?

_This documentation page is based on the [fast-check documentation](https://fast-check.dev/docs/introduction/what-is-property-based-testing/), and used under that project's MIT license._

## Examples versus properties

The vast majority of automated tests written today are "example-based" tests. Developers choose an exact set of values to input to the code being tested, and then they specify the exact outputs which are expected. This is true whether the tests in question are unit, integration, or end-to-end. The idea is that if developers can provide a sufficiently thorough set of examples of the code's desired behavior then the behavior of the code will be reliable.

In other words, example-based tests depend on very specific sets of inputs and expect very specific outcomes. When we write an example-based test we assert the way the code should behave on **one** set of inputs.

"Property-based" tests are different. A property-based test allows users to focus on the behaviors they want to assess, rather than the specific values required to assess them. A "property" is an assertion of a relationship between a code's input and output which should hold for **all** sets of inputs.

In presume a property is a function which takes randomly-generated inputs as an argument, uses them to perform a test, and then throws an error if the test fails.

## The property-based feature set

Property-based testing libraries enable this approach by providing three main features:

- Random input generation

- Multiple runs per test

- Counterexample shrinking

These are mostly orthogonal to the feature sets provided by most testing frameworks such as grouping tests into suites, performing assertions, installing mocks, etc. This makes property-based testing highly compatible with existing testing frameworks. Indeed, presume can be used in conjunction with any Java testing framework.

## Randomly-generated inputs

A property-based testing framework provides tools for generating arbitrary values of some given type. Presume refers to these as "generators". A generator may be very simple; for example, the `Integer` generator produces random integers. Simple generators can also be used as building blocks to make more complex generators.

## Large numbers of test runs

As stated previously, a property should hold for all valid input values. It is not generally possible to test every single value, so property-based testing libraries sample a very large number of inputs. The higher the number of inputs sampled, the more confidence we can have that the property always holds. Presume will sample 100 inputs per test by default. This value is configurable per-test.

## Counterexample shrinking

Tests are most useful if they both detect failures and help developers to diagnose the root cause of the failure. When presume detects a failed test it will provide the developer with a "counterexample" showing an input in which the test failed. Presume will attempt to make sure this counterexample is as small as possible. For example, if the test failed when given an array, presume will try removing elements and check whether the test still fails. As a result a failure caused by an array with dozens of elements may result in the developer being shown a failing input with only 2 or 3 items. This makes it much easier to diagnose and debug the root cause of failures, and is a major differentiator between property-based testing libraries like presume and more naive approaches to random input generation.