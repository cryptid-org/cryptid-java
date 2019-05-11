package cryptid.ibe.bonehfranklin;

import cryptid.ellipticcurve.point.affine.AffinePoint;
import cryptid.util.MessageDigestFactory;
import cryptid.ibe.domain.PrivateKey;
import cryptid.ibe.PrivateKeyGenerator;
import cryptid.ibe.domain.PublicParameters;

import java.math.BigInteger;
import java.util.Objects;

import static cryptid.ibe.util.HashUtils.hashToPoint;

/**
 * Boneh-Franklin (RFC 5091) implementation of {@link PrivateKeyGenerator}.
 */
final class BonehFranklinPrivateKeyGeneratorImpl extends PrivateKeyGenerator {
    private final MessageDigestFactory messageDigestFactory;

    BonehFranklinPrivateKeyGeneratorImpl(final PublicParameters publicParameters, final BigInteger masterSecret,
                                         final MessageDigestFactory messageDigestFactory) {
        super(publicParameters, masterSecret);

        this.messageDigestFactory = messageDigestFactory;
    }

    @Override
    public PrivateKey extract(final String identity) {
        Objects.requireNonNull(identity);

        //Let Q_id = HashToPoint(E, p, q, id, hashfcn)
        final AffinePoint qId = hashToPoint(publicParameters.getEllipticCurve(),
                publicParameters.getEllipticCurve().getFieldOrder(), publicParameters.getQ(), identity, messageDigestFactory.obtainInstance());

        //Let S_id = [s]Q_id
        return new PrivateKey(qId.multiply(masterSecret, publicParameters.getEllipticCurve()));
    }
}
