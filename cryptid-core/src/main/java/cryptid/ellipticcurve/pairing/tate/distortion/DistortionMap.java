package cryptid.ellipticcurve.pairing.tate.distortion;

import cryptid.ellipticcurve.point.affine.AffinePoint;
import cryptid.ellipticcurve.EllipticCurve;
import cryptid.ellipticcurve.point.complexaffine.ComplexAffinePoint;

import java.util.Objects;

/**
 * Abstract class for elliptic curve distortion maps that can be used to create linearly independent points. For
 * examples on distortion maps, see [Intro-to-IBE: p63.].
 *
 * @see <a href="https://dl.acm.org/citation.cfm?id=1370962" target="_blank">Introduction to Identity-Based Encryption</a>
 */
public abstract class DistortionMap {
    protected final EllipticCurve ellipticCurve;

    /**
     * Constructs a new distortion map.
     * @param ellipticCurve the elliptic curve to operate on
     * @throws NullPointerException if the elliptic curve is {@code null}.
     */
    public DistortionMap(EllipticCurve ellipticCurve) {
        this.ellipticCurve = Objects.requireNonNull(ellipticCurve);
    }

    /**
     * Applies the distortion map to the specified point.
     * @param point the point to apply the map to
     * @return the result of the distortion
     */
    public abstract ComplexAffinePoint apply(AffinePoint point);

    /**
     * Gets the elliptic curve of the distortion map.
     * @return the elliptic curve this map operates on.
     */
    public EllipticCurve getEllipticCurve() {
        return ellipticCurve;
    }
}
