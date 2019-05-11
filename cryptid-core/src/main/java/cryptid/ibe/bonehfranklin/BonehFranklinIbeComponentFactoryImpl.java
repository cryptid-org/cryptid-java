package cryptid.ibe.bonehfranklin;

import cryptid.ibe.*;
import cryptid.ibe.domain.PublicParameters;
import cryptid.ibe.exception.ComponentConstructionException;
import cryptid.ellipticcurve.pairing.tate.TatePairing;
import cryptid.ellipticcurve.pairing.tate.TatePairingFactory;
import cryptid.util.MessageDigestFactory;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

/**
 * Boneh-Franklin (RFC 5091) implementation of {@link IbeComponentFactory}.
 */
public class BonehFranklinIbeComponentFactoryImpl implements IbeComponentFactory {
    private final SecureRandom secureRandom;

    /**
     * Constructs a new instance.
     * @param secureRandom a cryptographically strong random source
     */
    public BonehFranklinIbeComponentFactoryImpl(final SecureRandom secureRandom) {
        this.secureRandom = Objects.requireNonNull(secureRandom);
    }

    @Override
    public IbeClient obtainClient(final PublicParameters publicParameters) throws ComponentConstructionException {
        final MessageDigestFactory messageDigestFactory;

        try {
            messageDigestFactory = MessageDigestFactory.forAlgorithm(publicParameters.getHashFunction());
        } catch (NoSuchAlgorithmException e) {
            throw new ComponentConstructionException(e);
        }

        TatePairing tatePairing = TatePairingFactory.INSTANCE.typeOneTatePairing(publicParameters.getEllipticCurve(), publicParameters.getQ());

        return new BonehFranklinIbeClientImpl(publicParameters, secureRandom, messageDigestFactory, tatePairing);
    }

    @Override
    public PrivateKeyGenerator obtainPrivateKeyGenerator(final PublicParameters publicParameters, final BigInteger masterSecret) throws ComponentConstructionException {
        final MessageDigestFactory messageDigestFactory;

        try {
            messageDigestFactory = MessageDigestFactory.forAlgorithm(publicParameters.getHashFunction());
        } catch (NoSuchAlgorithmException e) {
            throw new ComponentConstructionException(e);
        }

        return new BonehFranklinPrivateKeyGeneratorImpl(publicParameters, masterSecret, messageDigestFactory);
    }
}
