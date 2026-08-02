package tech.jnkr.presume.exceptions;

public class FlakyGeneratorException extends RuntimeException {
    public FlakyGeneratorException(Object originalValue, String innerException) {
        super(
                String.format(
                        """
                        Nondeterminism was detected in this property's generator! It produced a value once and then threw an exception on identical input.

                        Original value: %s

                        Exception thrown: %s
                        """,
                        originalValue, innerException));
    }
}
