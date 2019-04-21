package cryptid.complex

import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.LongStream

import static java.util.stream.Collectors.toList

/**
 * Tests for {@link Complex}.
 */
class ComplexSpec extends Specification {
    def "(0, 0) should act as the additive identity element for any p."() {
        given:
        def c = new Complex(4, 3)
        def p = BigInteger.valueOf(5L)

        expect:
        c == c.modAdd(Complex.ZERO, p)
        c == Complex.ZERO.modAdd(c, p)
    }

    def "modAdd should be commutative."() {
        given:
        def a = new Complex(4, 3)
        def b = new Complex(3, 1)
        def result = new Complex(2, 4)
        def p = BigInteger.valueOf(5L)

        expect:
        result == a.modAdd(b, p)
        result == b.modAdd(a, p)
    }

    def "additiveInverse should result in the additive inverse."() {
        given:
        def p = BigInteger.valueOf(5L)

        expect:
        result == c.additiveInverse(p)

        where:
        c | result
        new Complex(4, 3) | new Complex(1, 2)
        new Complex(0, 0) | new Complex(0, 0)
    }

    def "The power of (1, 0) is (1, 0) for any exponent and any p."() {
        given:
        def base = new Complex(1L)
        def exp = BigInteger.valueOf(5L)
        def p = BigInteger.valueOf(5L)

        expect:
        base == base.modPow(exp, p)
    }

    def "The modulo power of complex numbers should work well."() {
        given:
        def p = BigInteger.valueOf(7L)

        expect:
        result == base.modPow(exp, p)

        where:
        base | exp | result
        new Complex(4, 1) | BigInteger.valueOf(3L) | new Complex(3, 5)
        new Complex(0, 0) | BigInteger.valueOf(8L) | new Complex(0, 0)
        new Complex(4, 3) | BigInteger.valueOf(5L) | new Complex(6, 1)
    }

    def "GF_5 | modMulScalar should just work."() {
        given:
        def p = BigInteger.valueOf(5L)
        def value = new Complex(2L, 3L);

        expect:
        result == value.modMulScalar(s, p)

        where:
        s                           | result
        BigInteger.valueOf(0L) | new Complex(0, 0)
        BigInteger.valueOf(1L) | new Complex(2, 3)
        BigInteger.valueOf(2L) | new Complex(4, 1)
        BigInteger.valueOf(3L) | new Complex(1, 4)
        BigInteger.valueOf(4L) | new Complex(3, 2)
    }

    def "The multiplicative inverse of (1, 0) is (1, 0) for any p."() {
        given:
        def id = new Complex(1, 0)
        def p = BigInteger.valueOf(5L)

        expect:
        id == id.multiplicativeInverse(p)
    }

    @Unroll
    def "GF_7 | Multiplying (#c.real, #c.imaginary) with its inverse results in the identity element. "() {
        given:
        def p = BigInteger.valueOf(7L)
        def id = new Complex(1L)

        expect:
        id == c.modMul(c.multiplicativeInverse(p), p)

        where:
        c << produceAllComplexOverFiniteField(7)
    }

    private static List<Complex> produceAllComplexOverFiniteField(long p) {
        // Sure, there is the LongStream.range method, but that threw
        // IncompatibleClassChangeError.
        return LongStream.iterate(1, { it + 1})
                .limit(p * p - 1)
                .mapToObj({ n -> new Complex((long)(n / p), n % p)})
                .collect(toList())
    }

    def "The modulo of two Complex"() {
        expect:
        result == complex.mod(m)

        where:
        complex | m | result
        new Complex(17, 4) | new Complex(9, 7) | new Complex(1, 6)
        new Complex(170, -43)| new Complex(12, 3) | new Complex(5, 5)
        new Complex(0, 0) | new Complex(2, -1) | new Complex(0, 0)
        new Complex(69, 420) | new Complex(42, 42) | new Complex(27, 42)
    }
}
