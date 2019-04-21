package cryptid.ellipticcurve.point.complexaffine

import cryptid.complex.Complex
import cryptid.ellipticcurve.EllipticCurve
import cryptid.ellipticcurve.point.complexaffine.ComplexAffinePoint
import spock.lang.Specification

/**
 * Tests for {@link cryptid.ellipticcurve.point.complexaffine.ComplexAffinePoint}.
 */
class ComplexAffinePointSpec extends Specification {
    def "Multiplication should just work."() {
        given:
        def ec = new EllipticCurve(BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(5L))

        expect:
        result == p.multiply(n, ec)

        where:
        p                                  | n                      | result
        new ComplexAffinePoint(0, 0, 1, 0) | BigInteger.valueOf(2L) | new ComplexAffinePoint(0, 0, 4, 0)
        new ComplexAffinePoint(0, 0, 4, 0) | BigInteger.valueOf(2L) | new ComplexAffinePoint(0, 0, 1, 0)
        new ComplexAffinePoint(2, 0, 2, 0) | BigInteger.ZERO        | ComplexAffinePoint.INFINITY
    }

    def "Adding the same point to itself with y = 0 should result in infinity."() {
        given:
        def p = new ComplexAffinePoint(new Complex(1, 0), new Complex(0, 0))
        def ec = new EllipticCurve(BigInteger.ONE, BigInteger.ONE, BigInteger.TEN)

        when:
        def result = p.add(p, ec)

        then:
        result == ComplexAffinePoint.INFINITY
    }

    def "Adding infinity to infinity should result in infinity."() {
        given:
        def ec = new EllipticCurve(BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(5L))

        expect:
        ComplexAffinePoint.INFINITY.add(ComplexAffinePoint.INFINITY, ec) == ComplexAffinePoint.INFINITY
    }

    def "Infinity should act as the identity element for addition."() {
        given:
        def ec = new EllipticCurve(BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(5L))
        def p = new ComplexAffinePoint(new Complex(0, 0), new Complex(1, 0))

        expect:
        p == p.add(ComplexAffinePoint.INFINITY, ec)
        p == ComplexAffinePoint.INFINITY.add(p, ec)
    }

    def "Addition on non-special cases should work correctly."() {
        given:
        def ec = new EllipticCurve(BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(5L))

        expect:
        result == p1.add(p2, ec)

        where:
        p1                                 | p2                                 | result
        new ComplexAffinePoint(0, 0, 1, 0) | new ComplexAffinePoint(0, 0, 1, 0) | new ComplexAffinePoint(0, 0, 4, 0)
        new ComplexAffinePoint(0, 0 ,4, 0) | new ComplexAffinePoint(0, 0, 4, 0) | new ComplexAffinePoint(0, 0, 1, 0)
        new ComplexAffinePoint(4, 0, 0, 0) | new ComplexAffinePoint(0, 0, 4, 0) | new ComplexAffinePoint(2, 0, 3, 0)
        new ComplexAffinePoint(0, 0, 4, 0) | new ComplexAffinePoint(4, 0, 0, 0) | new ComplexAffinePoint(2, 0, 3, 0)
        new ComplexAffinePoint(0, 0, 1, 0) | new ComplexAffinePoint(0, 0, 4, 0) | ComplexAffinePoint.INFINITY
        new ComplexAffinePoint(2, 0, 2, 0) | new ComplexAffinePoint(0, 0, 4, 0) | new ComplexAffinePoint(4, 0, 0, 0)
        new ComplexAffinePoint(0, 0, 4, 0) | new ComplexAffinePoint(2, 0, 2, 0) | new ComplexAffinePoint(4, 0, 0, 0)
    }
}
