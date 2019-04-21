package cryptid.ibe.bonehfranklin

import groovy.transform.Immutable
import cryptid.ellipticcurve.TypeOneEllipticCurve
import cryptid.ellipticcurve.point.affine.AffinePoint
import cryptid.ellipticcurve.point.affine.generator.GenerationStrategyFactory;
import cryptid.ellipticcurve.point.affine.generator.Mod3GenerationStrategy;
import cryptid.ibe.domain.IbeSetup
import cryptid.ibe.domain.PublicParameters
import cryptid.ibe.domain.SecurityLevel
import cryptid.ibe.util.SolinasPrimeFactory
import spock.lang.Specification
import spock.lang.Unroll

import java.security.SecureRandom

class BonehFranklinIbeSpec extends Specification {
    private enum MessageLength {
        SHORT(10), MEDIUM(100), LONG(1000), HUGE(10000)

        private MessageLength(int length) {
            this.length = length
        }

        private int length
    }

    private enum IdLength {
        SHORT(10), MEDIUM(100), LONG(1000), HUGE(10000)

        private IdLength(int length) {
            this.length = length
        }

        private int length
    }

    def "RFC 5091 predefined extract should work correctly."() {
        given:
        def secureRandom = new SecureRandom()
        def identity = "Bob"
        def s = new BigInteger("749e52ddb807e0220054417e514742b05a0", 16)
        def p = new BigInteger("a6a0ffd016103ffffffffff595f002fe9ef195f002fe9efb", 16)
        def pointP = new AffinePoint(
                new BigInteger("6924c354256acf5a0ff7f61be4f0495b54540a5bf6395b3d", 16),
                new BigInteger("024fd8e2eb7c09104bca116f41c035219955237c0eac19ab", 16))
        def pointPpublic = new AffinePoint(
                new BigInteger("a68412ae960d1392701066664d20b2f4a76d6ee715621108", 16),
                new BigInteger("9e7644e75c9a131d075752e143e3f0435ff231b6745a486f", 16))
        def setup = new IbeSetup(
                new PublicParameters(TypeOneEllipticCurve.ofOrder(p), makeSolinas(140, 48), pointP, pointPpublic, "SHA-1"), s)

        def expectedPoint = new AffinePoint(
                new BigInteger("8212b74ea75c841a9d1accc914ca140f4032d191b5ce5501", 16),
                new BigInteger("950643d940aba68099bdcb40082532b6130c88d317958657", 16))

        def componentFactory = new BonehFranklinIbeComponentFactoryImpl(secureRandom)
        def client = componentFactory.obtainPrivateKeyGenerator(setup.publicParameters, s)

        when:
        def result = client.extract(identity)

        then:
        expectedPoint == result.data
    }

    def makeSolinas(int a, int b) {
        return BigInteger.valueOf(2L).pow(a)
                .subtract(BigInteger.valueOf(2L).pow(b))
                .subtract(BigInteger.ONE)
    }

    @Unroll
    def "Fresh IBE setup | Matching IDs | Security param: #testParameters.securityLevel"() {
        when:
        def plaintextOptional = runFreshIbe(testParameters.securityLevel, testParameters.message, testParameters.encryptId, testParameters.decryptId)

        then:
        plaintextOptional.isPresent()
        testParameters.message == plaintextOptional.get()

        where:
        testParameters << [
                generateTestParametersWithMatchingIds(20, SecurityLevel.LOWEST, MessageLength.SHORT.length, IdLength.SHORT.length),
                generateTestParametersWithMatchingIds(20, SecurityLevel.LOWEST, MessageLength.MEDIUM.length, IdLength.MEDIUM.length),
                generateTestParametersWithMatchingIds(20, SecurityLevel.LOWEST, MessageLength.LONG.length, IdLength.LONG.length),
                generateTestParametersWithMatchingIds(20, SecurityLevel.LOWEST, MessageLength.HUGE.length, IdLength.HUGE.length),

                generateTestParametersWithMatchingIds(5, SecurityLevel.LOW, MessageLength.SHORT.length, IdLength.SHORT.length),
                generateTestParametersWithMatchingIds(5, SecurityLevel.LOW, MessageLength.MEDIUM.length, IdLength.MEDIUM.length),
                generateTestParametersWithMatchingIds(5, SecurityLevel.LOW, MessageLength.LONG.length, IdLength.LONG.length),
                generateTestParametersWithMatchingIds(5, SecurityLevel.LOW, MessageLength.HUGE.length, IdLength.HUGE.length),

                generateTestParametersWithMatchingIds(1, SecurityLevel.MEDIUM, MessageLength.SHORT.length, IdLength.SHORT.length),
                generateTestParametersWithMatchingIds(1, SecurityLevel.MEDIUM, MessageLength.MEDIUM.length, IdLength.MEDIUM.length),
                generateTestParametersWithMatchingIds(1, SecurityLevel.MEDIUM, MessageLength.LONG.length, IdLength.LONG.length),
                generateTestParametersWithMatchingIds(1, SecurityLevel.MEDIUM, MessageLength.HUGE.length, IdLength.HUGE.length),
        ].flatten()
    }

    @Unroll
    def "Fresh IBE setup | Different IDs | Security param: #testParameters.securityLevel"() {
        when:
        def plaintext = runFreshIbe(testParameters.securityLevel, testParameters.message, testParameters.encryptId, testParameters.decryptId)

        then:
        !plaintext.isPresent()

        where:
        testParameters << [
                generateTestParametersWithDifferentIds(20, SecurityLevel.LOWEST, MessageLength.SHORT.length, IdLength.SHORT.length),
                generateTestParametersWithDifferentIds(20, SecurityLevel.LOWEST, MessageLength.MEDIUM.length, IdLength.MEDIUM.length),
                generateTestParametersWithDifferentIds(20, SecurityLevel.LOWEST, MessageLength.LONG.length, IdLength.LONG.length),
                generateTestParametersWithDifferentIds(20, SecurityLevel.LOWEST, MessageLength.HUGE.length, IdLength.HUGE.length),

                generateTestParametersWithDifferentIds(5, SecurityLevel.LOW, MessageLength.SHORT.length, IdLength.SHORT.length),
                generateTestParametersWithDifferentIds(5, SecurityLevel.LOW, MessageLength.MEDIUM.length, IdLength.MEDIUM.length),
                generateTestParametersWithDifferentIds(5, SecurityLevel.LOW, MessageLength.LONG.length, IdLength.LONG.length),
                generateTestParametersWithDifferentIds(5, SecurityLevel.LOW, MessageLength.HUGE.length, IdLength.HUGE.length),

                generateTestParametersWithDifferentIds(1, SecurityLevel.MEDIUM, MessageLength.SHORT.length, IdLength.SHORT.length),
                generateTestParametersWithDifferentIds(1, SecurityLevel.MEDIUM, MessageLength.MEDIUM.length, IdLength.MEDIUM.length),
                generateTestParametersWithDifferentIds(1, SecurityLevel.MEDIUM, MessageLength.LONG.length, IdLength.LONG.length),
                generateTestParametersWithDifferentIds(1, SecurityLevel.MEDIUM, MessageLength.HUGE.length, IdLength.HUGE.length),
        ].flatten()
    }

    def runFreshIbe(SecurityLevel securityLevel, String message, String encryptId, String decryptId) {
        final SecureRandom secureRandom = new SecureRandom()
        def solinasPrimeFactory = new SolinasPrimeFactory(secureRandom)
        GenerationStrategyFactory<Mod3GenerationStrategy> factory = { ec -> new Mod3GenerationStrategy(ec, secureRandom) }
        def initializer = new BonehFranklinIbeInitializer(secureRandom, solinasPrimeFactory, factory)
        def setup = initializer.setup(securityLevel)
        def componentFactory = new BonehFranklinIbeComponentFactoryImpl(secureRandom)

        def pkg = componentFactory.obtainPrivateKeyGenerator(setup.publicParameters, setup.masterSecret)
        def client = componentFactory.obtainClient(setup.publicParameters)

        def ciphertext = client.encrypt(message, encryptId)
        def privateKey = pkg.extract(decryptId)

        return client.decrypt(privateKey, ciphertext)
    }

    private static generateTestParametersWithMatchingIds(int count, SecurityLevel securityLevel, int messageLength, int idLength) {
        return (1..count).collect { IbeTestParameters.generateWithMatchingIds(securityLevel, messageLength, idLength) }
    }

    private static generateTestParametersWithDifferentIds(int count, SecurityLevel securityLevel, int messageLength, int idLength) {
        return (1..count).collect { IbeTestParameters.generateWithDifferentIds(securityLevel, messageLength, idLength) }
    }

    private static generateRandomString(int length) {
        def alphabet = (('a'..'z') + ('A'..'Z') + ('0'..'9')).join()

        new Random().with {
            (1..length).collect { alphabet[ nextInt( alphabet.length() ) ] }.join()
        }
    }

    @Immutable
    private static class IbeTestParameters {
        SecurityLevel securityLevel
        String message
        String encryptId
        String decryptId

        static IbeTestParameters generateWithMatchingIds(final SecurityLevel securityLevel, final int messageLength, final int idLength) {
            def message = generateRandomString(messageLength)
            def id = generateRandomString(idLength)

            return new IbeTestParameters(securityLevel, message, id, id)
        }

        static IbeTestParameters generateWithDifferentIds(final SecurityLevel securityLevel, final int messageLength, final int idLength) {
            def message = generateRandomString(messageLength)
            def encryptId = generateRandomString(idLength)
            // We are using idLength + 1 to ensure that id and badId will never be the same
            def decryptId = generateRandomString(idLength + 1)

            return new IbeTestParameters(securityLevel, message, encryptId, decryptId)
        }
    }
}
