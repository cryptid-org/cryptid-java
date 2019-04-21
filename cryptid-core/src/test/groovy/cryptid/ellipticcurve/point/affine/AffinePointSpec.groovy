package cryptid.ellipticcurve.point.affine

import cryptid.ellipticcurve.EllipticCurve
import cryptid.ellipticcurve.point.affine.AffinePoint
import spock.lang.Specification

/**
 * Tests for {@link AffinePoint}
 */
class AffinePointSpec extends Specification {
    def "Multiplication should just work."() {
        given:
        def ec = new EllipticCurve(BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(5L))

        expect:
        result == p.multiply(n, ec)

        where:
        p                     | n                      | result
        new AffinePoint(0, 1) | BigInteger.valueOf(2L) | new AffinePoint(0, 4)
        new AffinePoint(0, 4) | BigInteger.valueOf(2L) | new AffinePoint(0, 1)
        new AffinePoint(2, 2) | BigInteger.ZERO        | AffinePoint.INFINITY
    }

    def "Adding the same point to itself with y = 0 should result in infinity."() {
        given:
        def p = new AffinePoint(1, 0)
        def ec = new EllipticCurve(BigInteger.ONE, BigInteger.ONE, BigInteger.TEN)

        when:
        def result = p.add(p, ec)

        then:
        result == AffinePoint.INFINITY
    }

    def "Adding infinity to infinity should result in infinity."() {
        given:
        def ec = new EllipticCurve(BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(5L))

        expect:
        AffinePoint.INFINITY.add(AffinePoint.INFINITY, ec) == AffinePoint.INFINITY
    }

    def "Infinity should act as the identity element for addition."() {
        given:
        def ec = new EllipticCurve(BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(5L))

        expect:
        result == p1.add(p2, ec)

        where:
        p1                    | p2                    | result
        new AffinePoint(0, 1) | AffinePoint.INFINITY  | new AffinePoint(0, 1)
        AffinePoint.INFINITY  | new AffinePoint(0, 1) | new AffinePoint(0, 1)
    }

    def "Addition on non-special cases should work correctly."() {
        given:
        def ec = new EllipticCurve(BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(5L))

        expect:
        result == p1.add(p2, ec)

        where:
        p1                    | p2                    | result
        new AffinePoint(0, 1) | new AffinePoint(0, 1) | new AffinePoint(0, 4)
        new AffinePoint(0, 4) | new AffinePoint(0, 4) | new AffinePoint(0, 1)
        new AffinePoint(4, 0) | new AffinePoint(0, 4) | new AffinePoint(2, 3)
        new AffinePoint(0, 4) | new AffinePoint(4, 0) | new AffinePoint(2, 3)
        new AffinePoint(0, 1) | new AffinePoint(0, 4) | AffinePoint.INFINITY
        new AffinePoint(2, 2) | new AffinePoint(0, 4) | new AffinePoint(4, 0)
        new AffinePoint(0, 4) | new AffinePoint(2, 2) | new AffinePoint(4, 0)
    }
}
