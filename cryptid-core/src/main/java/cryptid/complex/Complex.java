package cryptid.complex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Immutable arbitrary precision complex number with integer-only real and imaginary parts.
 */
public final class Complex {
    /**
     * The complex number {@code (0 + 0i)}.
     */
    public static final Complex ZERO = new Complex(0L, 0L);

    private final BigInteger real;

    private final BigInteger imaginary;

    /**
     * Constructs a new Complex with the specified long real part and 0 imaginary part.
     * @param real the real part
     */
    public Complex (final long real) {
        this(real, 0L);
    }

    /**
     * Constructs a new Complex with the specified long real and long imaginary parts.
     * @param real the real part
     * @param imaginary the imaginary part
     */
    public Complex(final long real, final long imaginary) {
        this(BigInteger.valueOf(real), BigInteger.valueOf(imaginary));
    }

    /**
     * Constructs a new Complex with the specified BigInteger real part and 0 imaginary part.
     * @param real the real part
     */
    public Complex(final BigInteger real) {
        this(real, BigInteger.ZERO);
    }

    /**
     * Constructs a new Complex with the specified BigInteger real and BigInteger imaginary parts.
     * @param real the real part
     * @param imaginary the imaginary part
     */
    public Complex(final BigInteger real, final BigInteger imaginary) {
        this.real = Objects.requireNonNull(real);
        this.imaginary = Objects.requireNonNull(imaginary);
    }

    /**
     * Returns a Complex whose value is {@code ((this.real + other.real) mod p, (this.imaginary + other.imaginary) mod p)}.
     * @param other the value to be added to this Complex.
     * @param p the modulus.
     * @return the result of the addition
     */
    public Complex modAdd(final Complex other, final BigInteger p) {
        // a = (a_r, a_i) b = (b_r, b_i)
        // ((a_r + b_r) mod p, (a_i + b_i) mod p)
        return new Complex(real.add(other.real).mod(p),
                imaginary.add(other.imaginary).mod(p));
    }

    /**
     * Returns a Complex that is the additive inverse of this Complex with respect to the specified modulus.
     * @param p the modulus
     * @return the additive inverse of this Complex
     */
    public Complex additiveInverse(final BigInteger p) {
        // c = (c_r, c_i)
        // (-c_r, -c_i)
        return new Complex(real.negate().mod(p), imaginary.negate().mod(p));
    }

    /**
     * Returns a Complex whose value is {@code ((this.real + s) mod p, this.imaginry)}.
     * @param s a scalar BigInteger
     * @param p the modulus
     * @return the result of the addition
     */
    public Complex modAddScalar(final BigInteger s, final BigInteger p) {
        // c = (c_r, c_i)
        // ((s + c_r) mod p, c_i)
        return new Complex(s.add(real).mod(p), imaginary);
    }

    /**
     * Returns a Complex whose value is this Complex raised to the specified exponent modulo p.
     * @param exp the exponent
     * @param p the modulus
     * @return the value of the exponentiation
     */
    public Complex modPow(final BigInteger exp, final BigInteger p) {
        if (p.equals(BigInteger.ONE)) {
            return Complex.ZERO;
        }

        Complex result = new Complex(BigInteger.ONE);
        Complex baseCpy = new Complex(real.mod(p), imaginary.mod(p));
        BigInteger expCpy = exp;

        while (expCpy.compareTo(BigInteger.ZERO) > 0) {
            if (expCpy.mod(BigInteger.valueOf(2L)).equals(BigInteger.ONE)) {
                result = baseCpy.modMul(result, p);
            }

            expCpy = expCpy.shiftRight(1);
            baseCpy = baseCpy.modMul(baseCpy, p);
        }

        return result;
    }

    /**
     * Return a Complex whose value is {@code ((this.real * s) mod p, (this.imag * s) mod p)}.
     * @param s the scalar.
     * @param p the modulus.
     * @return the result of the multiplication.
     */
    public Complex modMulScalar(final BigInteger s, final BigInteger p) {
        // c = (c_r, c_i)
        // r = (s * c_r)
        // i = (s * c_i)
        final BigInteger r = s.multiply(real);
        final BigInteger imag = s.multiply(imaginary);

        // (r mod p, i mod p)
        return new Complex(r.mod(p), imag.mod(p));
    }

    /**
     * Returns a Complex whose value is
     * {@code ((this.real * other.real - this.imag * other.imag) mod p, (this.imag * other.real + this.real * other.imag) mod p)}.
     * @param other the value to multiply with.
     * @param p the modulus.
     * @return the result of the multiplication.
     */
    public Complex modMul(final Complex other, final BigInteger p) {
        // a = (a_r, a_i) b = (b_r, b_i)
        // r = (a_r * b_r - a_i * b_i)
        // i = (a_i * b_r + a_r * b_i)
        final BigInteger r = real.multiply(other.real).subtract(imaginary.multiply(other.imaginary));
        final BigInteger i = imaginary.multiply(other.real).add(real.multiply(other.imaginary));

        // (r mod p, i mod p)
        return new Complex(r.mod(p), i.mod(p));
    }

    /**
     * Calculates the multiplicate inverse of this Complex with respect to p.
     * @param p the modulus.
     * @throws ArithmeticException if this Complex has no multiplicative inverse
     * @return the multiplicative inverse.
     */
    public Complex multiplicativeInverse(final BigInteger p) {
        if (real.equals(BigInteger.ZERO) && imaginary.equals(BigInteger.ZERO)) {
            throw new ArithmeticException("(0, 0) does not have a multiplicative inverse!");
        }

        // c = (c_r, 0)
        // result = (c_r^{-1}, 0)
        if (imaginary.equals(BigInteger.ZERO)) {
            return new Complex(real.modInverse(p));
        }

        // c = (0, c_i)
        // result = (0, -(c_i^{-1}))
        if (real.equals(BigInteger.ZERO)) {
            return new Complex(BigInteger.ZERO, imaginary.modInverse(p).negate().mod(p));
        }

        // This is some handcrafted voodoo black magic stuff, that actually works.
        // If a number has a multiplicative inverse, then correctly calculates it, otherwise it reports an error.
        // Note, that here both c_r and c_i are guaranteed to be non-zero.
        final BigInteger realInv = real.modInverse(p);

        // First we try to calculate the imaginary part (denoted as y).
        // y =  -(c_i * c_r^{-1}) * (c_r + c_r^{-1} * c_i^2)^{-1}
        //                                  |
        //              This sum can be zero. If that's the case, then the number has no inverse.
        // The terms of the product are denoted as q and rInv.
        final BigInteger q = realInv.multiply(imaginary.negate().mod(p));
        final BigInteger r = real.add(realInv.multiply(imaginary.pow(2)));

        if (r.equals(BigInteger.ZERO)) {
            throw new ArithmeticException("The number has no multiplicative inverse!");
        }

        final BigInteger rInv = r.modInverse(p);
        final BigInteger y = q.multiply(rInv).mod(p);

        // x (the real part) can be calculated using y.
        // x = c_r^{-1} + y * c_r^{-1} * c_i
        // The second term of the sum is denoted as t.
        final BigInteger t = y.multiply(realInv).multiply(imaginary).mod(p);
        final BigInteger x = realInv.add(t).mod(p);

        return new Complex(x, y);
    }

    public Complex negate() {
        return new Complex(
          real.negate(),
          imaginary.negate()
        );
    }

    /**
     * Returns a Complex whose value is
     * {@code (this.real - other.real, this.imag - other.imag)}.
     * @param other the value to substract with.
     * @return the result of the substraction.
     */
    public Complex sub(final Complex other) {
        return new Complex(
            real.subtract(other.getReal()),
            imaginary.subtract(other.getImaginary())
        );
    }

    /**
     * Returns a Complex whose value is
     * {@code (this.real * other.real - this.imag * other.imag, this.imag * other.real + this.real * other.imag)}.
     * @param other the value to multiply with.
     * @return the result of the multiplication.
     */
    public Complex mul(final Complex other) {
        final BigInteger r = real.multiply(other.real).subtract(imaginary.multiply(other.imaginary));
        final BigInteger i = imaginary.multiply(other.real).add(real.multiply(other.imaginary));

        return new Complex(r, i);
    }

    /**
     * Returns a Complex whose value is
     * {@code floor((this.real * other.real + this.imag * other.imag) / (other.real * other.real + other.imag * other.imag), (this.imag * other.real - this.real * other.imag) / (other.real * other.real + other.imag * other.imag))}.
     * @param other the value to divide with.
     * @return the floored result of the division.
     */
    public Complex div(final Complex other) {
        BigDecimal realNum = new BigDecimal(
                real.multiply(other.getReal()).add(
                        imaginary.multiply(other.getImaginary())
                )
        );

        BigDecimal imaginaryNum = new BigDecimal(
                imaginary.multiply(other.getReal()).subtract(
                        real.multiply(other.getImaginary())
                )
        );

        BigDecimal denom = new BigDecimal(
                other.getReal().pow(2).add(
                        other.getImaginary().pow(2)
                )
        );

        return new Complex(
                realNum.divide(denom, BigDecimal.ROUND_FLOOR).toBigInteger(),
                imaginaryNum.divide(denom, BigDecimal.ROUND_FLOOR).toBigInteger()
        );
    }

    /**
     * Returns a Complex whose value is
     * {@code (this mod other)}.
     * @param other the value to modulo with.
     * @return the result of the modulo.
     */
    public Complex mod(final Complex other) {
        return this.sub(
                this.div(other).mul(other)
        );
    }

    /**
     * Returns a Complex whose value is this Complex raised to the specified exponent.
     * @param exp the exponent
     * @return the value of the exponentiation
     */
    public Complex pow(final long exp) {
        Complex result = new Complex(BigInteger.ONE);

        for(long i = 0; i < exp; i++) {
            result = result.mul(this);
        }

        return result;
    }

    /**
     * Returns the modulus comparison of two Complex.
     * @param other to which this Complex is to be compared
     * @return -1, 0, 1 as the modulus of the Complex less than, equal to, or greater than other
     */
    public int compareTo(final Complex other) {
        BigInteger notRlyModulusThis = real.pow(2).add(
                imaginary.pow(2)
        );

        BigInteger notRlyModulusOther = other.getReal().pow(2).add(
                other.getImaginary().pow(2)
        );

        return notRlyModulusThis.compareTo(notRlyModulusOther);
    }

    /**
     * Gets the real part.
     * @return the real part of this Complex
     */
    public BigInteger getReal() {
        return real;
    }

    /**
     * Gets the imaginary part.
     * @return the imaginary part of this Complex
     */
    public BigInteger getImaginary() {
        return imaginary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Complex complex = (Complex) o;
        return Objects.equals(real, complex.real) &&
                Objects.equals(imaginary, complex.imaginary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(real, imaginary);
    }

    @Override
    public String toString() {
        return "Complex{" + real + " + " + imaginary + "i}";
    }
}
