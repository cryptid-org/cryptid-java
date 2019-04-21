package cryptid.ibe.exception;

/**
 * Exception that might be produced during the creation of a new {@link cryptid.ibe.IbeClient} or a new
 * {@link cryptid.ibe.PrivateKeyGenerator} instance.
 */
public class ComponentConstructionException extends Exception {
    public ComponentConstructionException(String message) {
        super(message);
    }

    public ComponentConstructionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ComponentConstructionException(Throwable cause) {
        super(cause);
    }
}
