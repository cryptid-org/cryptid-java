package cryptid.ibe.util;

import cryptid.complex.Complex;

import java.math.BigInteger;

import static cryptid.util.BigIntegerUtils.byteLength;
import static cryptid.util.BigIntegerUtils.convertPositiveBigIntegerToByteArray;

public class CanonicalUtils {
    private CanonicalUtils() {
        /*
         * Cannot be constructed.
         */
    }

    /**
     * Takes an element v in F_p^k, and returns a canonical octet string of fixed length representing v.  The parameter
     * o MUST be either 0 or 1, and specifies the ordering of the encoding.
     *
     * [RFC 5091 - Algorithm 4.3.1]
     * @param p the order of the finite field
     * @param ordering the ordering of the encoding
     * @param v the number to get the canonical representation of
     * @return the canonical representation of v
     */
    public static byte[] canonical(BigInteger p, CanonicalOrdering ordering, Complex v) {
        final int outputSize = byteLength(p);

        final byte[] realBytes = convertPositiveBigIntegerToByteArray(v.getReal(), outputSize);
        final byte[] imagBytes = convertPositiveBigIntegerToByteArray(v.getImaginary(), outputSize);

        final byte[] result = new byte[outputSize * 2];

        if (ordering == CanonicalOrdering.REAL_FIRST) {
            System.arraycopy(realBytes, 0, result, 0, outputSize);
            System.arraycopy(imagBytes, 0, result, outputSize, outputSize);
        } else {
            System.arraycopy(imagBytes, 0, result, 0, outputSize);
            System.arraycopy(realBytes, 0, result, outputSize, outputSize);
        }

        return result;
    }

    /**
     * Specified the ordering in which the {@link #canonical(BigInteger, CanonicalOrdering, Complex)} method will
     * put the result.
     */
    public enum CanonicalOrdering {
        /**
         * The resulting byte array will contain the real part first.
         */
        REAL_FIRST,

        /**
         * The resulting byte array will contain the imaginary part first.
         */
        IMAGINARY_FIRST
    }
}
