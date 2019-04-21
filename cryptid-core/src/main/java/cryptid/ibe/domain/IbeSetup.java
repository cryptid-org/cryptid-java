package cryptid.ibe.domain;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Immutable class holding the public parameters and the master secret needed to operate an IBE system.
 */
public class IbeSetup {
    private final PublicParameters publicParameters;
    private final BigInteger masterSecret;

    /**
     * Constructs a new instance using the specified settings.
     * @param publicParameters the public parameters
     * @param masterSecret the master secret
     */
    public IbeSetup(PublicParameters publicParameters, BigInteger masterSecret) {
        this.publicParameters = Objects.requireNonNull(publicParameters);
        this.masterSecret = Objects.requireNonNull(masterSecret);
    }

    /**
     * Gets the public parameters.
     * @return the public parameters
     */
    public PublicParameters getPublicParameters() {
        return publicParameters;
    }

    /**
     * Gets the master secret.
     * @return the master secret
     */
    public BigInteger getMasterSecret() {
        return masterSecret;
    }
}
