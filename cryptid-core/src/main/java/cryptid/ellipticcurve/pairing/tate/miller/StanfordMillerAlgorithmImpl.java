package cryptid.ellipticcurve.pairing.tate.miller;

import cryptid.complex.Complex;
import cryptid.ellipticcurve.point.affine.AffinePoint;
import cryptid.ellipticcurve.EllipticCurve;
import cryptid.ellipticcurve.point.complexaffine.ComplexAffinePoint;

import java.math.BigInteger;

/**
 * Class implementing the Miller's algorithm as it's written in the
 * <a href="https://crypto.stanford.edu/pbc/notes/ep/miller.html" target="_blank">Stanford Pairing-based Cryptography Notes</a>.
 */
public final class StanfordMillerAlgorithmImpl extends MillerAlgorithm {

    public StanfordMillerAlgorithmImpl(final EllipticCurve ellipticCurve, final BigInteger subgroupOrder) {
        super(ellipticCurve, subgroupOrder);
    }

    /**
     * Computes the Tate pairing using the Stanford PBC Miller's algorithm.
     * @param p a point of {@code }E[r]}
     * @param q a point linearly independent from {@code p}
     * @return the result of the pairing
     */
    @Override
    public Complex evaluate(final AffinePoint p, final ComplexAffinePoint q) {
        // Mostly, we stick to the notation used on the site, however in some places this is not possible,
        // because we want to write idiomatic Java.
        // Therefore, here the "<->" symbol is used to mark changes in the notation.
        // P <-> p
        // Q <-> q
        // V <-> v
        // Throughout the comments, TeX code is used for maths.

        // 1. Set f = 1 and V = P
        // f = 1
        // V = P
        PairingContext context = PairingContext.initialContext(ellipticCurve, p, q);

        // t <-> n.bitCount()
        // 2. for i = t - 1 to 0 do:
        for (int i = subgroupOrder.bitCount() - 1; i >= 0; --i) {
            context = doubleStep(context);

            // 2.2 if l_i = 1 then
            if (subgroupOrder.testBit(i)) {
                context = addStep(context);
            }
        }

        return context.f;
    }

    private PairingContext doubleStep(final PairingContext context) {
        //  2.1
        // f = f^{2} \frac{g_{V, V}(Q)}{g_{2V, -2V}(Q)}
        final AffinePoint doubleV = context.v.add(context.v, context.ec);
        final Complex gVVQ = evaluateTangent(context.ec, context.v, context.q);
        final Complex g2VMinus2VQ = evaluateVertical(context.ec, doubleV, context.q);
        final Complex g2VMinus2VQInv = g2VMinus2VQ.multiplicativeInverse(context.ec.getFieldOrder());
        final Complex frac = gVVQ.modMul(g2VMinus2VQInv, context.ec.getFieldOrder());
        Complex f = context.f.modMul(context.f, context.ec.getFieldOrder());
        f = f.modMul(frac, context.ec.getFieldOrder());

        // V = 2V
        return context.adjustWith(f, doubleV);
    }

    private PairingContext addStep(final PairingContext context) {
        // 2.2.1
        // f = f \frac{g_{V, P}(Q)}{g_{V + P, -(V + P)}(Q)}
        final AffinePoint vPlusP = context.v.add(context.p, context.ec);
        final Complex gVPQ = evaluateLine(context.ec, context.v, context.p, context.q);
        final Complex gVPlusQ = evaluateVertical(context.ec, vPlusP, context.q);
        final Complex gVPlusQInv = gVPlusQ.multiplicativeInverse(context.ec.getFieldOrder());
        final Complex frac = gVPQ.modMul(gVPlusQInv, context.ec.getFieldOrder());
        final Complex f = context.f.modMul(frac, context.ec.getFieldOrder());

        // V = V + P
        return context.adjustWith(f, vPlusP);
    }

    /**
     * Evaluates the divisor of a tangent on a Type-One elliptic curve.
     * @param ec the elliptic curve to operate on
     * @param a a point in {@code E(F_p)}
     * @param b a point {@code E(F_p^2)}
     * @return an element of {@code F_p^2} that is the divisor of the line tangent to {@code A} evaluated at {@code B}
     * @see <a href="https://tools.ietf.org/html/rfc5091#section-3.4.1" target="_blank">RFC 5091 - Algorithm 3.4.2</a>
     */
    private Complex evaluateTangent(final EllipticCurve ec, final AffinePoint a, final ComplexAffinePoint b) {
        // ----
        // | Argument checks
        // ----
        if (ComplexAffinePoint.isInfinity(b)) {
            throw new IllegalArgumentException("B must not be infinity!");
        }

        // ----
        // | Special cases
        // ----
        if (AffinePoint.isInfinity(a)) {
            return new Complex(1L, 0L);
        }

        if (a.getY().equals(BigInteger.ZERO)) {
            return evaluateVertical(ec, a, b);
        }

        // ----
        // | Line computation
        // ----
        // a = -3 * x_A^2
        final BigInteger threeAddInv = BigInteger.valueOf(-3L).mod(ec.getFieldOrder());
        final BigInteger aprime = a.getX().pow(2).multiply(threeAddInv).mod(ec.getFieldOrder());

        // b = 2 * y_A
        final BigInteger bprime = a.getY().multiply(BigInteger.valueOf(2L)).mod(ec.getFieldOrder());

        // c = -b * y_A - a * x_A
        final BigInteger bAddInv = bprime.negate().mod(ec.getFieldOrder());
        final BigInteger bAddInvyA = bAddInv.multiply(a.getY()).mod(ec.getFieldOrder());
        final BigInteger axA = aprime.multiply(a.getX()).mod(ec.getFieldOrder());
        final BigInteger axAaddInv = axA.negate().mod(ec.getFieldOrder());
        final BigInteger c = bAddInvyA.add(axAaddInv).mod(ec.getFieldOrder());

        // ----
        // | Evaluation at B
        // ----
        // r = a * x_B + b * y_B + c
        final Complex axB = b.getX().modMulScalar(aprime, ec.getFieldOrder());
        final Complex byB = b.getY().modMulScalar(bprime, ec.getFieldOrder());
        return axB.modAdd(byB, ec.getFieldOrder()).modAddScalar(c, ec.getFieldOrder());
    }

    /**
     * Evaluates the divisor of a vertical line on a Type-One elliptic curve.
     *
     * [RFC 5091 - Algorithm 3.4.1]
     * @param ec the elliptic curve to operate on
     * @param a a point in {@code E(F_p)}
     * @param b a point {@code E(F_p^2)}
     * @return an element of {@code F_p^2} that is the divisor of the vertical line going through
     *         {@code A} evaluated at {@code B}
     * @see <a href="https://tools.ietf.org/html/rfc5091#section-3.4.1" target="_blank">RFC 5091 - Algorithm 3.4.2</a>
     */
    private Complex evaluateVertical(final EllipticCurve ec, final AffinePoint a, final ComplexAffinePoint b) {
        // r = x_B - x_A
        final BigInteger xAaddInv = a.getX().negate().mod(ec.getFieldOrder());
        return b.getX().modAddScalar(xAaddInv, ec.getFieldOrder());
    }

    /**
     * Evaluates the divisor of a line on a Type-One elliptic curve.
     * @param ec the elliptic curve to operate on
     * @param a a point in {@code E(F_p)}
     * @param aprime a point in {@code E(F_o)}
     * @param b a point in {@code E(F_p^2)}
     * @return an element of {@code F_p^2} that is the divisor of the line going through
     *         {@code A'} and {@code A''} evaluated at {@code B}
     * @see <a href="https://tools.ietf.org/html/rfc5091#section-3.4.1" target="_blank">RFC 5091 - Algorithm 3.4.3</a>
     */
    private Complex evaluateLine(final EllipticCurve ec, final AffinePoint a, final AffinePoint aprime, final ComplexAffinePoint b) {
        // ----
        // | Argument checks
        // ----
        if (ComplexAffinePoint.isInfinity(b)) {
            throw new IllegalArgumentException("B must not be infinity!");
        }

        // ----
        // | Special cases
        // ----
        if (AffinePoint.isInfinity(a)) {
            return evaluateVertical(ec, aprime, b);
        }

        final AffinePoint aPlusAPrime = a.add(aprime, ec);
        if (AffinePoint.isInfinity(aprime) || AffinePoint.isInfinity(aPlusAPrime)) {
            return evaluateVertical(ec, a, b);
        }

        if (a.equals(aprime)) {
            return evaluateTangent(ec, a, b);
        }

        // ----
        // | Line computation
        // ----
        // a = y_A' - y_A''
        final BigInteger linea = a.getY().subtract(aprime.getY()).mod(ec.getFieldOrder());

        // b = x_A'' - x_A'
        final BigInteger lineb = aprime.getX().subtract(a.getX()).mod(ec.getFieldOrder());

        // c = -b * y_A' - a * x_A'
        final BigInteger linebaddinv = lineb.negate().mod(ec.getFieldOrder());
        final BigInteger q = linebaddinv.multiply(a.getY()).mod(ec.getFieldOrder());
        final BigInteger t = linea.multiply(a.getX()).mod(ec.getFieldOrder());
        final BigInteger taddinv = t.negate().mod(ec.getFieldOrder());
        final BigInteger linec = q.add(taddinv).mod(ec.getFieldOrder());

        // ----
        // | Evaluation at B
        // ----
        // r = a * x_B + b * y_B + c
        final Complex axb = b.getX().modMulScalar(linea, ec.getFieldOrder());
        final Complex byb = b.getY().modMulScalar(lineb, ec.getFieldOrder());
        return axb.modAdd(byb.modAddScalar(linec, ec.getFieldOrder()), ec.getFieldOrder());
    }

    private static final class PairingContext {
        private final Complex f;
        private final AffinePoint v;

        private final EllipticCurve ec;
        private final AffinePoint p;
        private final ComplexAffinePoint q;

        private static PairingContext initialContext(final EllipticCurve ec, final AffinePoint p, final ComplexAffinePoint q) {
            // We are in the degree two extension field of the original field, so the identity element
            // (with respect to multiplication) is (1, 0).
            // f = 1
            return new PairingContext(new Complex(1L, 0L), p, ec, p, q);
        }

        private PairingContext(final Complex f, AffinePoint v, final EllipticCurve ec, final AffinePoint p, final ComplexAffinePoint q) {
            this.f = f;
            this.v = v;
            this.ec = ec;
            this.p = p;
            this.q = q;
        }

        private PairingContext adjustWith(final Complex f, final AffinePoint v) {
            return new PairingContext(f, v, this.ec, this.p, this.q);
        }
    }
}
