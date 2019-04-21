package cryptid.ellipticcurve.pairing.miller

import cryptid.complex.Complex
import cryptid.ellipticcurve.TypeOneEllipticCurve
import cryptid.ellipticcurve.pairing.tate.TatePairing
import cryptid.ellipticcurve.pairing.tate.distortion.XiDistortionMap
import cryptid.ellipticcurve.pairing.tate.miller.StanfordMillerAlgorithmImpl
import cryptid.ellipticcurve.point.affine.AffinePoint
import spock.lang.Specification
import spock.lang.Unroll

class StanfordMillerAlgorithmImplSpec extends Specification {
    @Unroll
    def "GF_131 | E(GF_131)[11] | P = (98, 58) | Modified Tate-pairing should just work."() {
        given:
        def q = BigInteger.valueOf(11L)
        def k = 2
        def ec = TypeOneEllipticCurve.ofOrder(BigInteger.valueOf(131L))

        def a = new AffinePoint(98, 58)
        def b = a.multiply(BigInteger.valueOf((long)n), ec)

        def millerAlgorithm = new StanfordMillerAlgorithmImpl(ec, q)
        def distortionMap = new XiDistortionMap(ec)
        def pairing = new TatePairing(millerAlgorithm, distortionMap, k)

        expect:
        result == pairing.performPairing(a, b)

        where:
        n  | result
        1  | new Complex(28, 93)
        2  | new Complex(126, 99)
        3  | new Complex(85, 80)
        4  | new Complex(49, 58)
        5  | new Complex(39, 24)
        6  | new Complex(39, 107)
        7  | new Complex(49, 73)
        8  | new Complex(85, 51)
        9  | new Complex(126, 32)
        10 | new Complex(28, 38)
        11 | new Complex(1, 0)
    }

    def "RFC 5091"() {
        given:
        def qprime = new BigInteger("fffffffffffffffffffffffffffbffff", 16)
        def pprime = new BigInteger("bffffffffffffffffffffffffffcffff3", 16)
        def k = 2
        def ec = TypeOneEllipticCurve.ofOrder(pprime)

        def a_x = new BigInteger("489a03c58dcf7fcfc97e99ffef0bb4634", 16)
        def a_y = new BigInteger("510c6972d795ec0c2b081b81de767f808", 16)
        def b_x = new BigInteger("40e98b9382e0b1fa6747dcb1655f54f75", 16)
        def b_y = new BigInteger("b497a6a02e7611511d0db2ff133b32a3f", 16)
        def a = new AffinePoint(a_x, a_y)
        def b = new AffinePoint(b_x, b_y)

        def millerAlgorithm = new StanfordMillerAlgorithmImpl(ec, qprime)
        def distortionMap = new XiDistortionMap(ec)
        def pairing = new TatePairing(millerAlgorithm, distortionMap, k)

        def expected_a = new BigInteger("8b2cac13cbd422658f9e5757b85493818", 16)
        def expected_b = new BigInteger("bc6af59f54d0a5d83c8efd8f5214fad3c", 16)
        def expected = new Complex(expected_a, expected_b)

        when:
        def result = pairing.performPairing(a, b)

        then:
        result == expected
    }
}

