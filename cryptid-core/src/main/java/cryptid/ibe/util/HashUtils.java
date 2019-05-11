package cryptid.ibe.util;

import cryptid.ellipticcurve.TypeOneEllipticCurve;
import cryptid.ellipticcurve.point.affine.AffinePoint;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Utility class providing hash operations as described in the RFC 5091.
 */
public class HashUtils {
    private HashUtils() {
        /*
         * Cannot be constructed.
         */
    }

    /**
     * Takes an identity string id, the description of a subgroup of prime order q in E(F_p) or E(F_p^2), and
     * a cryptographic hash function hashfcn and returns a point Q_id of order q in E(F_p) or E(F_p^2).
     * @param ec the elliptic curve to operate on
     * @param p the order of the finite field
     * @param q the order of the subgroup
     * @param id the identity string
     * @param hashFunction a cryptographically strong hash function
     * @return a point of order q
     * @see <a href="https://tools.ietf.org/html/rfc5091#section-4.4.1" target="_blank">RFC 5091 - Algorithm 4.4.1</a>
     */
    public static AffinePoint hashToPoint(TypeOneEllipticCurve ec, BigInteger p, BigInteger q, String id, MessageDigest hashFunction) {
        //Let y = HashToRange(id, p, hashfcn), an element of F_p
        BigInteger y = hashToRange(id.getBytes(), p, hashFunction);

        //Let x = (y^2 - 1)^((2 * p - 1) / 3) modulo p, an element of F_p
        BigInteger x = y.pow(2).subtract(BigInteger.ONE).modPow(
                BigInteger.valueOf(2L).multiply(p).subtract(BigInteger.ONE).divide(BigInteger.valueOf(3L)),
                p
        );

        //Let Q' = (x, y), a non-zero point in E(F_p)
        AffinePoint qPrime = new AffinePoint(x, y);

        //Let Q = [(p + 1) / q ]Q', a point of order q in E(F_p)
        return qPrime.multiply(p.add(BigInteger.ONE).divide(q), ec);
    }

    /**
     * Takes a string s, an integer n, and a cryptographic hash function hashfcn as input and returns an integer
     * in the range 0 to n - 1 by cryptographic hashing.  The input n MUST  be less than 2^(hashlen), where hashlen
     * is the number of octets comprising the output of the hash function hashfcn.  HashToRange is based on Merkle's
     * method for hashing, which is provably as secure as the underlying hash function hashfcn.
     * @param s a string
     * @param p a positive integer
     * @param hashFunction a cryptographically strong hash function
     * @return returns an integer in the appropriate range
     * @see <a href="https://tools.ietf.org/html/rfc5091#section-4.1.1" target="_blank">RFC 5091 - Algorithm 4.1.1</a>
     */
    public static BigInteger hashToRange(byte[] s, BigInteger p, MessageDigest hashFunction) {
        //Let hashlen be the number of octets comprising the output of hashfcn
        int hashLen = hashFunction.getDigestLength();

        //Let v_0 = 0
        BigInteger v = BigInteger.ZERO;

        //Let h_0 = 0x00...00, a string of null octets with a length of hashlen
        byte[] h = new byte[hashLen];

        //For i = 1 to 2
        for(int i = 1; i < 3; i++) {
            //Let t_i = h_(i - 1) || s, which is the (|s| + hashlen)- octet
            //string concatenation of the strings h_(i - 1) and s
            byte[] t = new byte[h.length + s.length];
            System.arraycopy(h, 0, t, 0, h.length);
            System.arraycopy(s, 0, t, h.length, s.length);

            //Let h_i = hashfcn(t_i), which is a hashlen-octet string
            //resulting from the hash algorithm hashfcn on the input t_i
            hashFunction.update(t);
            h = hashFunction.digest();

            //Let a_i = Value(h_i) be the integer in the range 0 to
            //256^hashlen - 1 denoted by the raw octet string h_i
            //interpreted in the unsigned big-endian convention
            BigInteger a = new BigInteger(1, h);

            //Let v_i = 256^hashlen * v_(i - 1) + a_i
            v = BigInteger.valueOf(256L).pow(hashLen).multiply(v).add(a);
        }

        return v.mod(p);
    }

    /**
     * Takes an integer b, a string p, and a cryptographic hash function hashfcn as input and returns a b-octet
     * pseudo-random string r as output.  The value of b MUST be less than or equal to the number of bytes in the output
     * of hashfcn.  HashBytes is based on Merkle's method for hashing, which is provably as secure as the
     * underlying hash function hashfcn.
     * @param b the length of the output
     * @param p a string
     * @param hashFunction a cryptographically strong hash function
     * @return pseudorandom bytes
     * @see <a href="https://tools.ietf.org/html/rfc5091#section-4.2.1" target="_blank">RFC 5091 - Algorithm 4.2.1</a>
     */
    public static byte[] hashBytes(int b, byte[] p, MessageDigest hashFunction) {
        byte[] result = new byte[b];

        //Let hashlen be the number of octets comprising the output of hashfcn
        int hashLen = hashFunction.getDigestLength();

        //Let K = hashfcn(p)
        hashFunction.update(p);
        byte[] k = hashFunction.digest();

        //Let h_0 = 0x00...00, a string of null octets with a length of hashlen
        byte[] h = new byte[hashLen];

        //Let l = Ceiling(b / hashlen)
        int l = (int)Math.ceil((double)b / (double)hashLen);

        int generatedOctets = 0;
        boolean didGenerateEnough = false;
        //For each i in 1 to l
        for(int i = 1; i <= l && !didGenerateEnough; i++) {
            //Let h_i = hashfcn(h_(i - 1))
            hashFunction.update(h);
            h = hashFunction.digest();

            //Let r_i = hashfcn(h_i || K), where h_i || K is the (2 *
            //hashlen)-octet concatenation of h_i and K
            //Let r = LeftmostOctets(b, r_1 || ... || r_l), i.e., r is formed as
            //the concatenation of the r_i, truncated to the desired number of octets
            byte[] concat = new byte[2 * hashLen];
            System.arraycopy(h, 0, concat, 0, h.length);
            System.arraycopy(k, 0, concat, h.length, k.length);
            hashFunction.update(concat);
            byte[] resultPart = hashFunction.digest();
            for(int j = 0; j < resultPart.length; j++) {
                if(generatedOctets + j < b) {
                    result[generatedOctets + j] = resultPart[j];
                } else {
                    didGenerateEnough = true;
                    break;
                }
            }
            generatedOctets += resultPart.length;
        }

        return result;
    }
}
