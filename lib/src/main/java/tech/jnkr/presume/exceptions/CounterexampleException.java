package tech.jnkr.presume.exceptions;

public class CounterexampleException extends RuntimeException {
    public final int shrinkCount;
    public final String seed;
    public final Object counterexample;

    public CounterexampleException(
            int shrinkCount, String seed, Object counterexample, String inner) {
        super(
                String.format(
                        """
                        Property failed! Shrunk %d times.

                        Seed:
                        %s

                        Minimal counterexample:
                        %s

                        Inner exception:
                        %s
                        """,
                        shrinkCount, seed, counterexample, inner));
        this.shrinkCount = shrinkCount;
        this.seed = seed;
        this.counterexample = counterexample;
    }
}
