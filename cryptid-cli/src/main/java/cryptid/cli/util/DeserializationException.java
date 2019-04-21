package cryptid.cli.util;

public final class DeserializationException extends Exception {
    private static final String MESSAGE = "Could not deserialize input.";

    public DeserializationException(Throwable cause) {
        super(MESSAGE, cause);
    }
}
