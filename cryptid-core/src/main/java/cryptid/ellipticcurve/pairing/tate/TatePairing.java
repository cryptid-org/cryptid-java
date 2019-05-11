package cryptid.ellipticcurve.pairing.tate;

import cryptid.complex.Complex;
import cryptid.ellipticcurve.pairing.tate.distortion.DistortionMap;
import cryptid.ellipticcurve.pairing.tate.miller.MillerAlgorithm;
import cryptid.ellipticcurve.point.affine.AffinePoint;
import cryptid.ellipticcurve.EllipticCurve;
import cryptid.ellipticcurve.point.complexaffine.ComplexAffinePoint;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Class representing the Tate-pairing operation with a specific Miller's Algorithm implementation and
 * distortion map.
 */
public class TatePairing {
    private static final int MINIMAL_EMBEDDING_DEGREE = 2;

    private final MillerAlgorithm millerAlgorithm;
    private final DistortionMap distortionMap;
    private final int embeddingDegree;
    private final EllipticCurve ellipticCurve;
    private final BigInteger subgroupOrder;

    /**
     * Constructs a new instance.
     * @param millerAlgorithm Miller's algorithm implementation to use
     * @param distortionMap the distortion map to use
     */
    TatePairing(final MillerAlgorithm millerAlgorithm, final DistortionMap distortionMap, final int embeddingDegree) {
        if (!millerAlgorithm.getEllipticCurve().equals(distortionMap.getEllipticCurve())) {
            throw new IllegalArgumentException("The elliptic curves of the distortion map and the Miller's algorithm must be the same!");
        }

        if (embeddingDegree < MINIMAL_EMBEDDING_DEGREE) {
            throw new IllegalArgumentException("The embedding degree must be at least " + MINIMAL_EMBEDDING_DEGREE);
        }

        this.millerAlgorithm = Objects.requireNonNull(millerAlgorithm);
        this.distortionMap = Objects.requireNonNull(distortionMap);
        this.embeddingDegree = embeddingDegree;
        this.ellipticCurve = millerAlgorithm.getEllipticCurve();
        this.subgroupOrder = millerAlgorithm.getSubgroupOrder();
    }

    /**
     * Performs the Tate pairing on the specified points.
     * @param a point to perform the pairing on
     * @param b point to perform the pairing on
     * @return the result of the pairing
     */
    public Complex performPairing(final AffinePoint a, final AffinePoint b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);

        final ComplexAffinePoint bprime = distortionMap.apply(b);

        if (ComplexAffinePoint.isInfinity(bprime)) {
            return new Complex(1L);
        }

        final Complex f = millerAlgorithm.evaluate(a, bprime);

        return finalExponentiation(f);
    }

    private Complex finalExponentiation(final Complex f) {
        final BigInteger exponent = ellipticCurve.getFieldOrder().pow(embeddingDegree).subtract(BigInteger.ONE).divide(subgroupOrder);

        return f.modPow(exponent, ellipticCurve.getFieldOrder());
    }
}
