package cryptid.ibe;

import cryptid.ibe.domain.CipherTextTuple;
import cryptid.ibe.domain.PrivateKey;
import cryptid.ibe.domain.PublicParameters;

import java.util.Optional;

/**
 * Base class for classes that provide IBE encryption and decryption.
 */
public abstract class IbeClient {
    protected final PublicParameters publicParameters;

    /**
     * Constructs a new client.
     * @param publicParameters the public parameters of the IBE system
     */
    public IbeClient(PublicParameters publicParameters) {
        this.publicParameters = publicParameters;
    }

    /**
     * Encrypts the specified message using the specified identity.
     * @param message the message to encrypt
     * @param identity the identity to encrypt with
     * @return the ciphertext result of the encryption
     */
    public abstract CipherTextTuple encrypt(String message, String identity);

    /**
     * Decrypts the specified ciphertext with the specified private key. If the decryption if successful,
     * an Optional with the result is returned. However, if the decryption fails, then an empty Optional
     * is returned.
     * @param privateKey the private key
     * @param ciphertext the ciphertext to decrypt
     * @return an Optional with the plaintext result of the decryption or an empty Optional on failure
     */
    public abstract Optional<String> decrypt(PrivateKey privateKey, CipherTextTuple ciphertext);

    /**
     * Gets the public parameters.
     * @return the public parameters
     */
    public PublicParameters getPublicParameters() {
        return publicParameters;
    }
}
