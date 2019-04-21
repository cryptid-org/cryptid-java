package cryptid.ibe;

import cryptid.ibe.domain.IbeSetup;
import cryptid.ibe.domain.SecurityLevel;
import cryptid.ibe.exception.SetupException;

/**
 * Interface for classes that can newInstance valid setup settings of an IBE system.
 */
public interface IbeInitializer {
    /**
     * Constructs a new configuration that can be used to operate an IBE system.
     * @param securityLevel the desired security level
     * @return a new set of IBE configuration
     * @throws SetupException if the settings can not be produced
     */
    IbeSetup setup(SecurityLevel securityLevel) throws SetupException;
}
