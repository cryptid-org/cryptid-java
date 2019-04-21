package cryptid.ibe;

import cryptid.ibe.domain.PublicParameters;
import cryptid.ibe.exception.ComponentConstructionException;

import java.math.BigInteger;

/**
 * Interface for classes providing IBE system specific client and private key generator instances.
 */
public interface IbeComponentFactory {
    /**
     * Returns a client that uses the specified public parameters to encrypt and decrypt data. Implementations
     * may return the same instance on subsequent calls for the same public parameters.
     * @param publicParameters the public parameters of the IBE system
     * @return a client using thte specified public parameters
     * @throws ComponentConstructionException if the client cannot be constructed
     */
    IbeClient obtainClient(PublicParameters publicParameters) throws ComponentConstructionException;

    /**
     * Returns a private key generator that uses the specified data to extract private keys. Implementations may
     * return the same instance on subsequent calls for the same public parameters and the same master secret.
     * @param publicParameters the public parameters of the IBE system
     * @param masterSecret the master secret of the IBE system
     * @return a private key generator
     * @throws ComponentConstructionException if the private key generator cannot be constructed
     */
    PrivateKeyGenerator obtainPrivateKeyGenerator(PublicParameters publicParameters, BigInteger masterSecret) throws ComponentConstructionException;
}
