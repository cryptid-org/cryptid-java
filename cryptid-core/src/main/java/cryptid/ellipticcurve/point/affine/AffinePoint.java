package cryptid.ellipticcurve.point.affine;

import cryptid.ellipticcurve.EllipticCurve;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.math.BigInteger.ZERO;

/**
 * Represents an immutable affine point with {@link BigInteger} coordinates.
 */
public final class AffinePoint {
    /**
     * The infinity point of an elliptic curve. Note, that using affine coordinates, it's not possible to
     * represent infinity. Therefore when comparing or returning infinity, this value or the {@link #isInfinity(AffinePoint)}
     * method should be used.
     */
    public static final AffinePoint INFINITY = new AffinePoint(BigInteger.ZERO, BigInteger.ZERO);

    /**
     * Checks if the given point os equal to infinity.
     * @param p the point to check.
     * @return whether p is equal to infinity.
     */
    public static boolean isInfinity(final AffinePoint p) {
        return p == INFINITY;
    }

    private final BigInteger x;

    private final BigInteger y;

    /**
     * Constructs a new point using the specified long coordinates.
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public AffinePoint(final long x, final long y) {
        this(BigInteger.valueOf(x), BigInteger.valueOf(y));
    }

    /**
     * Constructs a new point.
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public AffinePoint(final BigInteger x, final BigInteger y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Multiplies the point with a scalar.
     * @param s the scalar to multiply with
     * @param ec the elliptic curve to operate over
     * @return the result of the multiplication
     */
    public AffinePoint wNAFMultiply(final BigInteger s, final EllipticCurve ec) {
        //Lets be w = 4 because it seems to be the best choice so 2^w = 16
        BigInteger d = s;
        BigInteger twoPowW = BigInteger.valueOf(16L);
        BigInteger twoPowWSubOne = BigInteger.valueOf(8L);

        List<AffinePoint> prePoints = new ArrayList<>();
        List<Integer> naf = new ArrayList<>();

        prePoints.add(new AffinePoint(x, y.negate().mod(ec.getFieldOrder())));
        prePoints.add(new AffinePoint(x, y));

        for(int i = 3; i < twoPowWSubOne.intValue(); i+= 2) {
            AffinePoint tmp = this.multiply(BigInteger.valueOf(i), ec);
            prePoints.add(new AffinePoint(tmp.x, tmp.y.negate().mod(ec.getFieldOrder())));
            prePoints.add(new AffinePoint(tmp.x, tmp.y));
        }

        int i = 0;
        while(d.compareTo(BigInteger.ZERO) > 0) {
            if(d.mod(BigInteger.valueOf(2L)).compareTo(BigInteger.ONE) == 0) {
                BigInteger mod = d.mod(twoPowW);
                if(mod.compareTo(twoPowWSubOne) >= 0) {
                    naf.add(mod.subtract(twoPowW).intValue());
                } else {
                    naf.add(mod.intValue());
                }
                d = d.subtract(BigInteger.valueOf(naf.get(i)));
            } else {
                naf.add(0);
            }
            d = d.divide(BigInteger.valueOf(2L));
            i++;
        }

        AffinePoint pointQ = AffinePoint.INFINITY;
        for(int j = i - 1; j >= 0; j--) {
            pointQ = pointQ.doubl(ec);
            int chosen = naf.get(j);
            if(chosen != 0) {
                int index = chosen>0?chosen:Math.abs(chosen) - 1;
                pointQ = pointQ.add(prePoints.get(index), ec);
            }
        }

        return pointQ;
    }

    /**
     * Multiplies the point with a scalar.
     * @param s the scalar to multiply with
     * @param ec the elliptic curve to operate over
     * @return the result of the multiplication
     */
    public AffinePoint multiply(final BigInteger s, final EllipticCurve ec) {
        if (s.equals(BigInteger.ZERO)) {
            return AffinePoint.INFINITY;
        }

        if (AffinePoint.isInfinity(this)) {
            return AffinePoint.INFINITY;
        }

        AffinePoint pointN = new AffinePoint(x, y);
        AffinePoint pointQ = AffinePoint.INFINITY;

        final String d = s.toString(2);

        for (int i = d.length() - 1; i  >= 0; i--) {
            if (d.charAt(i) == '1') {
                pointQ = pointQ.add(pointN, ec);
            }

            pointN = pointN.doubl(ec);
        }

        return pointQ;
    }

    public AffinePoint doubl(final EllipticCurve ec) {
        if (this == AffinePoint.INFINITY) {
            return AffinePoint.INFINITY;
        }

        // Having p1 equal to p2, we need to check if the y coordinate is 0, because it would
        // result in a divide-by-zero error.
        if (this.y.equals(ZERO)) {
            return AffinePoint.INFINITY;
        }

        // m = (3*x_1^2 + a) / 2 * y_1
        final BigInteger inv = this.y.multiply(BigInteger.valueOf(2L)).modInverse(ec.getFieldOrder());
        BigInteger m = this.x.pow(2).multiply(BigInteger.valueOf(3L)).add(ec.getA())
                .multiply(inv);

        // x = m^2 - x_1 - x_2
        final BigInteger xn = m.pow(2).subtract(this.x).subtract(this.x);
        // y = m(x_1 - x) - y_1
        final BigInteger yn = m.multiply(this.x.subtract(xn)).subtract(this.y);

        return new AffinePoint(xn.mod(ec.getFieldOrder()), yn.mod(ec.getFieldOrder()));
    }

    /**
     * Adds another affine point to this point.
     *
     * Implementation of Algorithm 3.1 in [Intro-to-IBE].
     * @param other the other point to add
     * @param ec the curve to operate on
     * @return the result of the addition
     */
    public AffinePoint add(final AffinePoint other, final EllipticCurve ec) {
        // Infinity is the identity element for elliptic curve addition.
        if (this == AffinePoint.INFINITY) {
            return other;
        }

        if (other == AffinePoint.INFINITY) {
            return this;
        }

        // The slope of the line.
        final BigInteger m;

        if (this.equals(other)) {
            return doubl(ec);
        } else {
            // Having equal X coordinates (and different points) a divide-by-zero error would
            // happen -> return infinity.
            //
            // Note, that in the pseudocode, this check is the first step of the algorithm
            // which is clearly wrong.
            if (this.x.equals(other.x)) {
                return AffinePoint.INFINITY;
            }

            // m = (y_2 - y_1) / (x_2 - x_1)
            final BigInteger inv = other.x.subtract(this.x).modInverse(ec.getFieldOrder());
            m = other.y.subtract(this.y).multiply(inv);
        }

        // x = m^2 - x_1 - x_2
        final BigInteger xn = m.pow(2).subtract(this.x).subtract(other.x);
        // y = m(x_1 - x) - y_1
        final BigInteger yn = m.multiply(this.x.subtract(xn)).subtract(this.y);

        return new AffinePoint(xn.mod(ec.getFieldOrder()), yn.mod(ec.getFieldOrder()));
    }

    /**
     * Gets the x coordinate
     * @return the x coordinate
     */
    public BigInteger getX() {
        return x;
    }

    /**
     * Gets the y coordinate
     * @return the y coordinate
     */
    public BigInteger getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AffinePoint point = (AffinePoint) o;

        // Infinity should only be equal to itself.
        // Sadly, Infinity can only be represented with projective coordinates, so
        // we need this hack in affine points.
        return Objects.equals(x, point.x) &&
                Objects.equals(y, point.y) &&
                !isInfinity(this);
    }

    @Override
    public int hashCode() {
        if (isInfinity(this)) {
            return -1;
        } else {
            return Objects.hash(x, y);
        }
    }

    @Override
    public String toString() {
        return "AffinePoint{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
