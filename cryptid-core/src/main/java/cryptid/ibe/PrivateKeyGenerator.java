package cryptid.ibe;

import cryptid.ibe.domain.PrivateKey;
import cryptid.ibe.domain.PublicParameters;

import java.math.BigInteger;

/**
 * Base class for private key generators that can extract private keys for corresponding identities.
 */
public abstract class PrivateKeyGenerator {
    protected final PublicParameters publicParameters;
    protected final BigInteger masterSecret;

    /**
     * Constructs a new private key generator.
     * @param publicParameters the public parameters
     * @param masterSecret the master secret
     */
    public PrivateKeyGenerator(PublicParameters publicParameters, BigInteger masterSecret) {
        this.publicParameters = publicParameters;
        this.masterSecret = masterSecret;
    }

    /**
     * Extract the private key corresponding to the specified identity.
     * @param identity the identity to get the private key of
     * @return the private key of the specified identity
     */
    public abstract PrivateKey extract(String identity);

    /**
     * Gets the public parameters.
     * @return the public parameters
     */
    public PublicParameters getPublicParameters() {
        return publicParameters;
    }
}
