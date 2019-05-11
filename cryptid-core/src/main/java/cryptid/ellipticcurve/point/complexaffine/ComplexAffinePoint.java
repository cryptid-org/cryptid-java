package cryptid.ellipticcurve.point.complexaffine;

import cryptid.complex.Complex;
import cryptid.ellipticcurve.EllipticCurve;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Represents an immutable affine point with {@link Complex} coordinates.
 */
public final class ComplexAffinePoint {
    /**
     * The infinity point of an elliptic curve. Note, that using affine coordinates, it's not possible to
     * represent infinity. Therefore when comparing or returning infinity, this value or the {@link #isInfinity(ComplexAffinePoint)}
     * method should be used.
     */
    public static final ComplexAffinePoint INFINITY = new ComplexAffinePoint(new Complex(0, 0), new Complex(0, 0));

    /**
     * Checks if the given point os equal to infinity.
     * @param p the point to check.
     * @return whether p is equal to infinity.
     */
    public static boolean isInfinity(final ComplexAffinePoint p) {
        return p == INFINITY;
    }

    private final Complex x;

    private final Complex y;

    /**
     * Constructs a new point using the given long coordinates.
     * @param xr x coordinate real part
     * @param xi x coordinate imaginary part
     * @param yr y coordinate real part
     * @param yi y coordinate imaginary part
     */
    public ComplexAffinePoint(final long xr, final long xi, final long yr, final long yi) {
        this(new Complex(xr, xi), new Complex(yr, yi));
    }

    /**
     * Constructs a new point
     * @param x the x coordinate
     * @param y the y coordinate
     * @throws NullPointerException if either of the coordinates is {@code null}
     */
    public ComplexAffinePoint(final Complex x, final Complex y) {
        this.x = Objects.requireNonNull(x);
        this.y = Objects.requireNonNull(y);
    }

    /**
     * Multiplies the point with a scalar.
     * @param s the scalar to multiply with
     * @param ec the elliptic curve to operate over
     * @return the result of the multiplication
     */
    public ComplexAffinePoint multiply(final BigInteger s, final EllipticCurve ec) {
        if (s.equals(BigInteger.ZERO)) {
            return ComplexAffinePoint.INFINITY;
        }

        if (ComplexAffinePoint.isInfinity(this)) {
            return ComplexAffinePoint.INFINITY;
        }

        ComplexAffinePoint pointN = new ComplexAffinePoint(x, y);
        ComplexAffinePoint pointQ = ComplexAffinePoint.INFINITY;

        final String d = s.toString(2);

        for (int i = d.length() - 1; i  >= 0; i--) {
            if (d.charAt(i) == '1') {
                pointQ = pointQ.add(pointN, ec);
            }

            pointN = pointN.add(pointN, ec);
        }

        return pointQ;
    }

    /**
     * Adds this point to another affine point.
     *
     * Implementation of Algorithm 3.1 in [Intro-to-IBE].
     * @param other the point to add this point to.
     * @param ec the curve to operate over
     * @return the result of the addition.
     */
    public ComplexAffinePoint add(final ComplexAffinePoint other, final EllipticCurve ec) {
        // Infinity is the identity element for elliptic curve addition.
        if (this == ComplexAffinePoint.INFINITY) {
            return other;
        }

        if (other == ComplexAffinePoint.INFINITY) {
            return this;
        }

        // The slope of the line.
        final Complex m;

        final Complex x1AddInv = x.additiveInverse(ec.getFieldOrder());
        final Complex y1AddInv = y.additiveInverse(ec.getFieldOrder());

        if (this.equals(other)) {
            // Having p1 equal to p2, we need to check if the y coordinate is 0, because it would
            // result in a divide-by-zero error.
            if (y.equals(Complex.ZERO)) {
                return ComplexAffinePoint.INFINITY;
            }

            // m = (3*x_1^2 + a) / 2 * y_1
            final Complex denom = y
                    .modMulScalar(BigInteger.valueOf(2L), ec.getFieldOrder())
                    .multiplicativeInverse(ec.getFieldOrder());
            final Complex num = x
                    .modMul(x, ec.getFieldOrder())
                    .modMulScalar(BigInteger.valueOf(3L), ec.getFieldOrder())
                    .modAddScalar(ec.getA(), ec.getFieldOrder());

            m = num.modMul(denom, ec.getFieldOrder());
        } else {
            // Having equal X coordinates (and different points) a divide-by-zero error would
            // happen -> return infinity.
            //
            // Note, that in the pseudocode, this check is the first step of the algorithm
            // which is clearly wrong.
            if (x.equals(other.x)) {
                return ComplexAffinePoint.INFINITY;
            }

            // m = (y_2 - y_1) / (x_2 - x_1)
            final Complex denom = other.x
                    .modAdd(x1AddInv, ec.getFieldOrder())
                    .multiplicativeInverse(ec.getFieldOrder());

            final Complex num = other.y.modAdd(y1AddInv, ec.getFieldOrder());

            m = num.modMul(denom, ec.getFieldOrder());
        }

        // x = m^2 - x_1 - x_2
        final Complex x2AddInv = other.x.additiveInverse(ec.getFieldOrder());
        final Complex mSquared = m.modMul(m, ec.getFieldOrder());

        final Complex xn = mSquared.modAdd(x1AddInv.modAdd(x2AddInv, ec.getFieldOrder()), ec.getFieldOrder());

        // y = m(x_1 - x) - y_1
        final Complex xAddInv = xn.additiveInverse(ec.getFieldOrder());
        final Complex q = this.x.modAdd(xAddInv, ec.getFieldOrder());
        final Complex r = m.modMul(q, ec.getFieldOrder());

        final Complex yn = r.modAdd(y1AddInv, ec.getFieldOrder());

        return new ComplexAffinePoint(xn, yn);
    }

    /**
     * Gets the x coordinate.
     * @return the x coordinate.
     */
    public Complex getX() {
        return x;
    }

    /**
     * Gets the y coordinate.
     * @return the y coordinate.
     */
    public Complex getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplexAffinePoint that = (ComplexAffinePoint) o;

        // Infinity should only be equal to itself.
        // Sadly, Infinity can only be represented with projective coordinates, so
        // we need this hack in affine points.
        return Objects.equals(x, that.x) &&
                Objects.equals(y, that.y) &&
                !isInfinity(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "ComplexAffinePoint{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
