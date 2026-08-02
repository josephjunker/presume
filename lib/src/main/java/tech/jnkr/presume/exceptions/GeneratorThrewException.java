package tech.jnkr.presume.exceptions;

public class GeneratorThrewException extends RuntimeException {
    public GeneratorThrewException(String innerExn) {
        super(
                String.format(
                        """
                        The generator for this property threw while trying to generate the property's input.
                        Inner exception:
                        %s
                        """,
                        innerExn));
    }
}
