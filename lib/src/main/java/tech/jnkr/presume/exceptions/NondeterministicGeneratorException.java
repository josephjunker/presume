package tech.jnkr.presume.exceptions;

public class NondeterministicGeneratorException extends RuntimeException {
    public NondeterministicGeneratorException(Throwable innerException) {
        super(
                "Nondeterminism was detected in the generator: repeated executions produced"
                        + " different results.",
                innerException);
    }
}
