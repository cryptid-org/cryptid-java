package cryptid.ibe.exception;

/**
 * Exception that might be produced when creating a new IBE setup.
 */
public class SetupException extends Exception {
    public SetupException(String message) {
        super(message);
    }

    public SetupException(String message, Throwable cause) {
        super(message, cause);
    }
}
