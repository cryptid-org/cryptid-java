package cryptid.util;

import java.math.BigInteger;
import java.util.Random;

/**
 * Utility class with various {@link BigInteger} helper methods.
 */
public final class BigIntegerUtils {
    private BigIntegerUtils() {
        /*
         * Cannot be constructed.
         */
    }

    /**
     * Returns the number of bytes needed to represent a BigInteger excluding a sign bit.
     * @param i the BigInteger to determine the length of
     * @return the byte length of the BigInteger
     */
    public static int byteLength(final BigInteger i) {
        return i.bitLength() / 8 + ((i.bitLength() % 8 == 0) ? 0 : 1);
    }

    /**
     * Converts the specified positive BigInteger to a byte array of the specified length. The resulting array will be
     * zero-padded.
     * @param i the BigInteger to convert
     * @param length the length of the
     * @throws IllegalArgumentException if {@code length} is smaller than the minimum number of bytes needed to represent
     *         the BigInteger
     * @return a byte array representation of the BigInteger
     */
    public static byte[] convertPositiveBigIntegerToByteArray(final BigInteger i, final int length) {
        byte[] result = new byte[length];

        int effectiveLength = byteLength(i);

        if (effectiveLength > length) {
            throw new IllegalArgumentException("The specified length is smaller than the byte length of the BigInteger!");
        }

        byte[] intBytes = i.toByteArray();
        int fullLength = intBytes.length;
        int startOffset = length - effectiveLength;

        System.arraycopy(intBytes, fullLength - effectiveLength, result, startOffset, effectiveLength);

        return result;
    }

    /**
     * Produces a random BigInteger in the specified range.
     * @param from the lower bound of the range.
     * @param to the upper bound of the range
     * @param random the random source
     * @return a random BigInteger
     */
    public static BigInteger randomBigInteger(BigInteger from, BigInteger to, Random random) {
        return (new BigInteger(to.bitLength(), random)).mod(to.subtract(from)).add(from);
    }
}
