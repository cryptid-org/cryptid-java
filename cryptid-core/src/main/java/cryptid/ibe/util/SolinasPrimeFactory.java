package cryptid.ibe.util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;

/**
 * Class capable of producing Solinas primes.
 */
public class SolinasPrimeFactory {
    private static final int BIT_LENGTH_LOWER_LIMIT = 1;

    private static final int GENERATOR_CERTAINTY = 100;

    private static final int ATTEMPT_LOWER_LIMIT = 1;

    private final SecureRandom secureRandom;

    /**
     * Constructs a new instance using the specified random source.
     * @param secureRandom a cryptographically strong random generator
     * @throws NullPointerException if the random generator is {@code null}
     */
    public SolinasPrimeFactory(SecureRandom secureRandom) {
        this.secureRandom = Objects.requireNonNull(secureRandom);
    }

    /**
     * Generates a new Solinas prime of the specified length in the specified number of attempts or returns an
     * empty Optional if the generation fails.
     * @param numberOfBits the desired bit-length of the prime
     * @param attemptLimit the maximum number of attempts
     * @return an Optional of a new Solinas prime
     */
    public Optional<BigInteger> generate(final int numberOfBits, final int attemptLimit) {
        if (numberOfBits < BIT_LENGTH_LOWER_LIMIT) {
            throw new IllegalArgumentException("The number of bits must be at least " + BIT_LENGTH_LOWER_LIMIT);
        }

        if (attemptLimit < ATTEMPT_LOWER_LIMIT) {
            throw new IllegalArgumentException("Attempt limit must be at least " + ATTEMPT_LOWER_LIMIT);
        }

        int random = 1, lastrandom;
        boolean isPrimeGenerated = false;
        int attempts = 0;

        BigInteger prime = null;

        while ((attempts < attemptLimit) && !isPrimeGenerated) {
            lastrandom = random;

            random = secureRandom.nextInt(numberOfBits - lastrandom) + lastrandom;

            for (int i = random; i >= lastrandom; i--) {
                prime = BigInteger.valueOf(2L).pow(numberOfBits)
                        .subtract(BigInteger.valueOf(2L).pow(i))
                        .subtract(BigInteger.ONE);

                if(prime.isProbablePrime(GENERATOR_CERTAINTY)){
                    isPrimeGenerated = true;
                    break;
                }
            }

            ++attempts;
        }

        return (isPrimeGenerated ? Optional.of(prime) : Optional.empty());
    }
}
