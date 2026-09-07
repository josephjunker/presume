package tech.jnkr.presume.exceptions;

public class GeneratorStatsAssertionFailureException extends RuntimeException {
    private GeneratorStatsAssertionFailureException(String message) {
        super(message);
    }

    public static GeneratorStatsAssertionFailureException minimumFailed(
            String title, String bucketName, float actual, float minimum) {
        String message =
                String.format(
                        """
                        Assertion failed when checking generator statistics for category: %s
                        The %s bucket had a frequency beneath its minimum threshold.
                        Minimum allowed: %f
                        Actual: %f
                        """,
                        title, bucketName, actual, minimum);
        return new GeneratorStatsAssertionFailureException(message);
    }

    public static GeneratorStatsAssertionFailureException maximumFailed(
            String title, String bucketName, float actual, float maximum) {
        String message =
                String.format(
                        """
                        Assertion failed when checking generator statistics for category: %s
                        The %s bucket had a frequency above its maximum threshold.
                        Maximum allowed: %f
                        Actual: %f
                        """,
                        title, bucketName, actual, maximum);
        return new GeneratorStatsAssertionFailureException(message);
    }
}
