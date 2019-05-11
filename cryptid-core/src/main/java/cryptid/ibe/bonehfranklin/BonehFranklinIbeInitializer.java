package cryptid.ibe.bonehfranklin;

import cryptid.ellipticcurve.TypeOneEllipticCurve;
import cryptid.ellipticcurve.point.affine.AffinePoint;
import cryptid.ellipticcurve.point.affine.generator.AffinePointGenerationStrategy;
import cryptid.ellipticcurve.point.affine.generator.GenerationStrategyFactory;
import cryptid.ibe.*;
import cryptid.ibe.domain.IbeSetup;
import cryptid.ibe.domain.PublicParameters;
import cryptid.ibe.domain.SecurityLevel;
import cryptid.ibe.exception.SetupException;
import cryptid.ibe.util.SolinasPrimeFactory;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;

import static cryptid.util.BigIntegerUtils.randomBigInteger;

/**
 * Boneh-Franklin (RFC-5091) implementation of {@link IbeInitializer}.
 */
public class BonehFranklinIbeInitializer implements IbeInitializer {
    private static final int PRIME_CERTAINTY = 100;
    private static final int PRIME_GENERATION_ATTEMPTS = 100;
    private static final int POINT_GENERATION_ATTEMPTS = 100;

    private final SecureRandom secureRandom;
    private final SolinasPrimeFactory primeFactory;
    private final GenerationStrategyFactory<?> generationStrategyFactory;

    public BonehFranklinIbeInitializer(final SecureRandom secureRandom, final SolinasPrimeFactory solinasPrimeFactory,
                                       final GenerationStrategyFactory<?> generationStrategyFactory) {
        this.secureRandom = Objects.requireNonNull(secureRandom);
        this.primeFactory = Objects.requireNonNull(solinasPrimeFactory);
        this.generationStrategyFactory = generationStrategyFactory;
    }

    @Override
    public IbeSetup setup(final SecurityLevel securityLevel) throws SetupException {
        // Construct the elliptic curve and its subgroup of interest
        // Select a random n_q-bit Solinas prime q
        Optional<BigInteger> qOptional = primeFactory.generate(securityLevel.getqLength(), PRIME_GENERATION_ATTEMPTS);

        if (!qOptional.isPresent()) {
            throw new SetupException("Could not generate Solinas prime!");
        }

        BigInteger q = qOptional.get();

        // Select a random integer r, such that p = 12 * r * q - 1 is an n_p-bit prime
        int lengthOfR = securityLevel.getpLength() - securityLevel.getqLength() - 3;
        BigInteger r, p, rBase = BigInteger.valueOf(2L).pow(lengthOfR);

        do {
            r = randomBigInteger(BigInteger.ZERO, rBase, secureRandom);
            p = BigInteger.valueOf(12L).multiply(r).multiply(q).subtract(BigInteger.ONE);
        } while (!p.isProbablePrime(PRIME_CERTAINTY));

        TypeOneEllipticCurve ec = TypeOneEllipticCurve.ofOrder(p);

        final AffinePointGenerationStrategy pointGenerationStrategy = generationStrategyFactory.newInstance(ec);

        // Select a point P of order q in E(F_p)
        AffinePoint pointP;
        do {
            Optional<AffinePoint> pointOpt = pointGenerationStrategy.generate(POINT_GENERATION_ATTEMPTS);

            if (!pointOpt.isPresent()) {
                throw new SetupException("Could not generate random point!");
            }

            AffinePoint pointPprime = pointOpt.get();
            pointP = pointPprime.multiply(BigInteger.valueOf(12L).multiply(r), ec);
        } while (AffinePoint.isInfinity(pointP));

        // Determine the master secret
        BigInteger s = randomBigInteger(BigInteger.valueOf(2L), q, secureRandom);

        // Determine the public parameters
        AffinePoint pointPpublic = pointP.multiply(s, ec);

        return new IbeSetup(new PublicParameters(ec, q, pointP, pointPpublic, securityLevel.getHashFunction()), s);
    }
}
