package cryptid;

import cryptid.ellipticcurve.point.affine.generator.GenerationStrategyFactory;
import cryptid.ellipticcurve.point.affine.generator.Mod3GenerationStrategy;
import cryptid.ibe.IbeClient;
import cryptid.ibe.IbeComponentFactory;
import cryptid.ibe.IbeInitializer;
import cryptid.ibe.IdentityBasedEncryption;
import cryptid.ibe.PrivateKeyGenerator;
import cryptid.ibe.bonehfranklin.BonehFranklinIbeComponentFactoryImpl;
import cryptid.ibe.bonehfranklin.BonehFranklinIbeInitializer;
import cryptid.ibe.domain.IbeSetup;
import cryptid.ibe.domain.SecurityLevel;
import cryptid.ibe.exception.SetupException;
import cryptid.ibe.util.SolinasPrimeFactory;

import java.security.SecureRandom;

/**
 * Class of static factories providing a convenient entry point to the CryptID library.
 */
public final class CryptID {
    private CryptID() {
        /*
         * Cannot be constructed.
         */
    }

    /**
     * Creates a new Boneh-Franklin IBE setup that can be used for encryption, decryption and private key
     * extraction.
     * @param securityLevel The desired security level of the setup.
     * @return a ready-to-use {@code IdentityBasedEncryption} instance
     * @throws SetupException if the IBE setup cannot be created
     */
    public static IdentityBasedEncryption setupBonehFranklin(final SecurityLevel securityLevel) throws SetupException {
        try {
            final SecureRandom secureRandom = SecureRandom.getInstanceStrong();
            final SolinasPrimeFactory solinasPrimeFactory = new SolinasPrimeFactory(secureRandom);

            final GenerationStrategyFactory<Mod3GenerationStrategy> generationStrategyFactory =
                    ellipticCurve -> new Mod3GenerationStrategy(ellipticCurve, secureRandom);

            final IbeInitializer initializer =
                    new BonehFranklinIbeInitializer(secureRandom, solinasPrimeFactory, generationStrategyFactory);

            final IbeSetup setup = initializer.setup(securityLevel);

            final IbeComponentFactory componentFactory = new BonehFranklinIbeComponentFactoryImpl(secureRandom);

            final IbeClient client = componentFactory.obtainClient(setup.getPublicParameters());

            final PrivateKeyGenerator privateKeyGenerator =
                    componentFactory.obtainPrivateKeyGenerator(setup.getPublicParameters(), setup.getMasterSecret());

            return new IdentityBasedEncryption(client, privateKeyGenerator);
        } catch (final Exception e) {
            throw new SetupException("Could not setup Boneh-Franklin IBE.", e);
        }
    }
}
