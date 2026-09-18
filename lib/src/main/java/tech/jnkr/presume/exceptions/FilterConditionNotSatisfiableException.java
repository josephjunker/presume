package tech.jnkr.presume.exceptions;

public class FilterConditionNotSatisfiableException extends RuntimeException {
    public FilterConditionNotSatisfiableException(int attempts) {
        super(
                String.format(
                        "Could not find a value which passed the filter after %d attempts",
                        attempts));
    }
}
