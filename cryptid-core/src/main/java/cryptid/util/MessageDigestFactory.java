package cryptid.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Factory class that can provide instances of the specified message digest algorithm.
 */
public final class MessageDigestFactory {
    private final String algorithm;

    /**
     * Constructs a new factory that can provide instances of the specified message digest algorithm.
     * @param algorithm the message digest algorithm
     * @return a new factory
     * @throws NoSuchAlgorithmException if there is no provider for the specified algorithm
     */
    public static MessageDigestFactory forAlgorithm(String algorithm) throws NoSuchAlgorithmException {
        // If it throws, it throws, that's it.
        MessageDigest.getInstance(algorithm);

        // Otherwise, we can safely assume, that subsequent calls will not throw either.
        return new MessageDigestFactory(algorithm);
    }

    private MessageDigestFactory(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Returns a new {@code MessageDigest} instance. Every invocation of this method is required to return
     * a fresh instance.
     * @return a {@code MessageDigest} instance.
     */
    public MessageDigest obtainInstance() {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not get message digest instance for the desired hash function!", e);
        }
    }

    /**
     * Gets the algorithm, this factory can produce {@link MessageDigest} instances for.
     * @return the algorithm
     */
    public String getAlgorithm() {
        return algorithm;
    }
}
