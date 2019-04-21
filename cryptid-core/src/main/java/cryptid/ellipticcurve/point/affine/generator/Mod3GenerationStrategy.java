package cryptid.ellipticcurve.point.affine.generator;

import cryptid.ellipticcurve.TypeOneEllipticCurve;
import cryptid.ellipticcurve.point.affine.AffinePoint;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Optional;

/**
 * Generation strategy that generates a point on the curve using the mod3 approach which attempts to solve the curve
 * equation by choosing a random y coordinate and solving for x.
 */
public final class Mod3GenerationStrategy extends AffinePointGenerationStrategy {
    public Mod3GenerationStrategy(TypeOneEllipticCurve ellipticCurve, SecureRandom secureRandom) {
        super(ellipticCurve, secureRandom);
    }

    @Override
    public Optional<AffinePoint> nextPoint() {
        final BigInteger y = randomBigInteger();

        final BigInteger bAddInv = ellipticCurve.getB().negate().mod(ellipticCurve.getFieldOrder());
        final BigInteger base =
                y.pow(2).add(bAddInv).mod(ellipticCurve.getFieldOrder());

        final BigInteger exp = ellipticCurve.getFieldOrder().multiply(BigInteger.valueOf(2L)).subtract(BigInteger.ONE).divide(BigInteger.valueOf(3L));

        final BigInteger x = base.modPow(exp, ellipticCurve.getFieldOrder());

        return Optional.of(new AffinePoint(x, y));
    }
}
