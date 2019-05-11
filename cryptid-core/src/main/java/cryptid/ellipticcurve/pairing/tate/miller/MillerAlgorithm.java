package cryptid.ellipticcurve.pairing.tate.miller;

import cryptid.complex.Complex;
import cryptid.ellipticcurve.point.affine.AffinePoint;
import cryptid.ellipticcurve.EllipticCurve;
import cryptid.ellipticcurve.point.complexaffine.ComplexAffinePoint;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Base class for implementations of Miller's algorithm.
 */
public abstract class MillerAlgorithm {
    protected final EllipticCurve ellipticCurve;

    protected final BigInteger subgroupOrder;

    public MillerAlgorithm(final EllipticCurve ellipticCurve, final BigInteger subgroupOrder) {
        this.ellipticCurve = Objects.requireNonNull(ellipticCurve);
        this.subgroupOrder = Objects.requireNonNull(subgroupOrder);
    }

    /**
     * Evaluates Miller's algorithm on the specified points.
     * @param p a point of E[subgroupOrder]
     * @param q a point linearly independent from p
     * @return the result of the pairing
     */
    public abstract Complex evaluate(AffinePoint p, ComplexAffinePoint q);

    public EllipticCurve getEllipticCurve() {
        return ellipticCurve;
    }

    public BigInteger getSubgroupOrder() {
        return subgroupOrder;
    }
}
