package cryptid.ellipticcurve;

import cryptid.ellipticcurve.point.affine.AffinePoint;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Immutable class representing an elliptic curve and a finite field over it.
 */
public class EllipticCurve {
    private final BigInteger a;

    private final BigInteger b;

    private final BigInteger fieldOrder;

    /**
     * Constructs a new elliptic curve of the form {@code y^2 = x^3 + ax + b} using the specified
     * coefficients.
     * @param a the {@code a} coefficient.
     * @param b the {@code b} coefficient.
     * @param fieldOrder the order of the finite field over the curve.
     * @throws NullPointerException if any of the arguments is {@code null}.
     */
    EllipticCurve(final BigInteger a, final BigInteger b, final BigInteger fieldOrder) {
        this.a = Objects.requireNonNull(a);
        this.b = Objects.requireNonNull(b);
        this.fieldOrder = Objects.requireNonNull(fieldOrder);
    }

    /**
     * Gets the a coefficient.
     * @return the a coefficient.
     */
    public BigInteger getA() {
        return a;
    }

    /**
     * Gets the b coefficient.
     * @return the b coefficient.
     */
    public BigInteger getB() {
        return b;
    }

    /**
     * Gets the order of the finite field.
     * @return the order of the finite field.
     */
    public BigInteger getFieldOrder() {
        return fieldOrder;
    }

    /**
     * Checks if the specified point is on the curve (satisfies the curve equation).
     * @param p the point to check
     * @throws NullPointerException if p is {@code null}
     * @return {@code true} if the point os on the curve, {@code false} otherwise.
     */
    public boolean isOnCurve(final AffinePoint p) {
        Objects.requireNonNull(p);

        final BigInteger rhs = p.getX().pow(3).add(p.getX().multiply(a)).add(b).mod(fieldOrder);
        final BigInteger lhs = p.getY().pow(2).mod(fieldOrder);

        return lhs.equals(rhs);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EllipticCurve that = (EllipticCurve) o;
        return Objects.equals(a, that.a) &&
                Objects.equals(b, that.b) &&
                Objects.equals(fieldOrder, that.fieldOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, fieldOrder);
    }
}
