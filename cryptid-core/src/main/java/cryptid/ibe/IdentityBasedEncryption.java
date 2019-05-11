package cryptid.ibe;

import cryptid.ibe.domain.CipherTextTuple;
import cryptid.ibe.domain.PrivateKey;

import java.util.Objects;
import java.util.Optional;

/**
 * Convenience class that can be used to perform encrypt, decrypt and extract operations.
 */
public class IdentityBasedEncryption {
    private final IbeClient client;
    private final PrivateKeyGenerator privateKeyGenerator;

    /**
     * Constructs a new instance using the specified components.
     * @param client the client that will be used for encryption and decryption
     * @param privateKeyGenerator the PKG which will provide extract capapbilities
     */
    public IdentityBasedEncryption(final IbeClient client, final PrivateKeyGenerator privateKeyGenerator) {
        this.client = Objects.requireNonNull(client);
        this.privateKeyGenerator = Objects.requireNonNull(privateKeyGenerator);
    }

    /**
     * Encrypts the specified message using the provided identity.
     * @param message the message to encrypt
     * @param identity the identity of the receiver
     * @return the ciphertext
     */
    public CipherTextTuple encrypt(final String message, final String identity) {
        return client.encrypt(message, identity);
    }

    /**
     * Decrypts the specified ciphertext with the specified private key. If the decryption if successful,
     * an Optional with the result is returned. However, if the decryption fails, then an empty Optional
     * is returned.
     * @param privateKey the private key
     * @param ciphertext the ciphertext to decrypt
     * @return an Optional with the plaintext result of the decryption or an empty Optional on failure
     */
    public Optional<String> decrypt(final PrivateKey privateKey, final CipherTextTuple ciphertext) {
        return client.decrypt(privateKey, ciphertext);
    }

    /**
     * Extracts the private key corresponding to the specified identity.
     * @param identity the identity whose private key should be extracted
     * @return the private key corresponding to the identity
     */
    public PrivateKey extract(final String identity) {
        return privateKeyGenerator.extract(identity);
    }
}
