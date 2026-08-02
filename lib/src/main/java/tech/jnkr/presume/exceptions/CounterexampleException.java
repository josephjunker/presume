package tech.jnkr.presume.exceptions;

public class CounterexampleException extends RuntimeException {
    public CounterexampleException(
            int shrinkCount, String seed, Object counterexample, Exception inner) {
        super(
                String.format(
                        """
                        Property failed! Shrunk %d times.

                        Seed: %s

                        Minimal counterexample:
                        %s\
                        """,
                        shrinkCount, seed, counterexample),
                inner);
    }
}
