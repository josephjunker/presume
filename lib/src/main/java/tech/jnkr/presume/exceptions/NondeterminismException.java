package tech.jnkr.presume.exceptions;

public class NondeterminismException extends RuntimeException {
    public NondeterminismException(Object firstRun, Throwable firstException, Object secondRun) {
        super(
                "Nondeterminism detected! A property failed once and succeeded once for the"
                        + " same test seed. Ensure that the same value was produced by the"
                        + " generator both times; if they are not identical then the problem is"
                        + " a nondeterministic generator. If they are identical then the"
                        + " problem is in the property or code under test."
                        + String.format(
                                "\n\nFirst produced value (which caused an exception):\n%s",
                                firstRun)
                        + String.format(
                                "\n\nSecond produced value (which passed the property):\n%s",
                                secondRun)
                        + "\n",
                firstException);
    }
}
