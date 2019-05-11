package cryptid.ellipticcurve.point.affine.generator;

import cryptid.ellipticcurve.TypeOneEllipticCurve;
import cryptid.ellipticcurve.point.affine.AffinePoint;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;

/**
 * Abstract class representing random point generation on a specific elliptic curve.
 */
public abstract class AffinePointGenerationStrategy {
    private static final int ATTEMPT_LOWER_LIMIT = 1;

    protected final TypeOneEllipticCurve ellipticCurve;

    protected final SecureRandom secureRandom;

    /**
     * Constructs a new generator which produces points on the specified curve.
     * @param ellipticCurve the curve to generate points on
     * @param secureRandom source of random values
     */
    AffinePointGenerationStrategy(final TypeOneEllipticCurve ellipticCurve, final SecureRandom secureRandom) {
        this.ellipticCurve = Objects.requireNonNull(ellipticCurve);
        this.secureRandom = Objects.requireNonNull(secureRandom);
    }

    /**
     * Generates a point on the elliptic curve.
     * @param attemptLimit the maximum number of attempts
     * @return a point on the curve or an empty Optional if no point was generated
     */
    public Optional<AffinePoint> generate(final int attemptLimit) {
        if (attemptLimit < ATTEMPT_LOWER_LIMIT) {
            throw new IllegalArgumentException("Attempt limit must be at least " + ATTEMPT_LOWER_LIMIT);
        }

        int attempts = 0;
        Optional<AffinePoint> result;

        do {
            result = nextPoint().filter(this::isOnCurve);

            if (result.isPresent()) {
                break;
            }

            ++attempts;
        } while (attempts < attemptLimit);

        return result;
    }

    protected abstract Optional<AffinePoint> nextPoint();

    protected final BigInteger randomBigInteger() {
        return (new BigInteger(ellipticCurve.getFieldOrder().bitLength(), secureRandom)).mod(ellipticCurve.getFieldOrder());
    }

    private boolean isOnCurve(final AffinePoint p) {
        final BigInteger ySquared = p.getY().pow(2).mod(ellipticCurve.getFieldOrder());
        final BigInteger xCubed = p.getX().pow(3).mod(ellipticCurve.getFieldOrder());

        final BigInteger ax = p.getX().multiply(ellipticCurve.getA()).mod(ellipticCurve.getFieldOrder());

        final BigInteger rhs = xCubed.add(ax).add(ellipticCurve.getB()).mod(ellipticCurve.getFieldOrder());

        return ySquared.compareTo(rhs) == 0;
    }
}
