package cryptid.ellipticcurve.pairing.tate.distortion;

import cryptid.complex.Complex;
import cryptid.ellipticcurve.pairing.tate.distortion.DistortionMap;
import cryptid.ellipticcurve.point.affine.AffinePoint;
import cryptid.ellipticcurve.TypeOneEllipticCurve;
import cryptid.ellipticcurve.point.complexaffine.ComplexAffinePoint;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Distortion map that calculates a complex \xi value and then multiplies the x coordinate of the point with it.
 * This is a specific implementation for Type-One elliptic curves.
 *
 * In the ordinary case (this always applies, for any curve with any prime):
 * <pre>
 * {@code
 * \phi(x, y) = (\xi \cdot x, y)
 * \xi \neq 1, \xi^{3} = 1
 * }
 * </pre>
 *
 * In this specific case (if the aforementioned conditions hold):
 * <pre>
 * {@code
 * \xi = \frac{p - 1}{2}(1 + 3^{\frac{p + 1}{4}}i)
 * }
 * </pre>
 */
public class XiDistortionMap extends DistortionMap {
    private final Complex xi;

    /**
     * Constructs a new xi distortion map over the specified curve.
     * @param ellipticCurve the elliptic curve to operate over
     */
    public XiDistortionMap(final TypeOneEllipticCurve ellipticCurve) {
        super(ellipticCurve);

        this.xi = precalculateXi();
    }

    @Override
    public ComplexAffinePoint apply(final AffinePoint point) {
        Objects.requireNonNull(point);

        if (AffinePoint.isInfinity(point)) {
            return ComplexAffinePoint.INFINITY;
        }

        // x' = x * xi
        // x in F_p | xi in F_p^2
        // Here we assume, that we have to convert x to F_p^2 and then perform the multiplication
        // according to the complex multiplication rules.
        final Complex xprime = xi.modMulScalar(point.getX(), ellipticCurve.getFieldOrder());

        // B' = (x', y) in F_p^2 x F_p
        // Technically, we have F_p^2 x F_p^2, but the second coordinate of y is always 0.
        return new ComplexAffinePoint(xprime, new Complex(point.getY(), BigInteger.ZERO));
    }

    private Complex precalculateXi() {
        // a_{xi} = (p - 1) / 2
        final BigInteger axi = ellipticCurve.getFieldOrder().subtract(BigInteger.ONE).divide(BigInteger.valueOf(2L));

        // b_{xi} = 3^{\frac{p + 1}{4}} mod p
        final BigInteger bxi = BigInteger.valueOf(3L).modPow(
                ellipticCurve.getFieldOrder().add(BigInteger.ONE).divide(BigInteger.valueOf(4L)),
                ellipticCurve.getFieldOrder()
        );

        // xi in F_p^2
        return (new Complex(BigInteger.ONE, bxi)).modMulScalar(axi, ellipticCurve.getFieldOrder());
    }
}
