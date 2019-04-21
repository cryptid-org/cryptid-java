package cryptid.ellipticcurve.pairing.tate;

import cryptid.ellipticcurve.TypeOneEllipticCurve;
import cryptid.ellipticcurve.pairing.tate.distortion.DistortionMap;
import cryptid.ellipticcurve.pairing.tate.distortion.XiDistortionMap;
import cryptid.ellipticcurve.pairing.tate.miller.MillerAlgorithm;
import cryptid.ellipticcurve.pairing.tate.miller.StanfordMillerAlgorithmImpl;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Factory class producing {@link TatePairing} instances.
 */
public enum TatePairingFactory {
    INSTANCE;

    private static final int EMBEDDING_DEGREE = 2;

    /**
     * Constructs a new Tate-pairing over a Type-One elliptic curve.
     * @param ellipticCurve the elliptic curve to operate on.
     * @param subgroupOrder the order of the subgroup, the pairing works on.
     * @throws IllegalArgumentException if the embedding degree is not equal to 2
     * @throws NullPointerException if either of the arguments is {@code null}
     * @return a new {@code TatePairing} instance
     */
    public TatePairing typeOneTatePairing(final TypeOneEllipticCurve ellipticCurve, final BigInteger subgroupOrder) {
        Objects.requireNonNull(ellipticCurve);
        Objects.requireNonNull(subgroupOrder);

        if (!checkIfEmbeddingDegreeIsAllowed(ellipticCurve, subgroupOrder, EMBEDDING_DEGREE)) {
            throw new IllegalArgumentException("The elliptic curve group described by the specified arguments does not have embedding degree of "
                    + EMBEDDING_DEGREE);
        }

        final MillerAlgorithm millerAlgorithm = new StanfordMillerAlgorithmImpl(ellipticCurve, subgroupOrder);
        final DistortionMap distortionMap = new XiDistortionMap(ellipticCurve);

        return new TatePairing(millerAlgorithm, distortionMap, EMBEDDING_DEGREE);
    }

    private boolean checkIfEmbeddingDegreeIsAllowed(final TypeOneEllipticCurve ellipticCurve, final BigInteger subgroupOrder, final int embeddingDegree) {
        final BigInteger num = ellipticCurve.getFieldOrder().pow(embeddingDegree).subtract(BigInteger.ONE);

        return num.mod(subgroupOrder).equals(BigInteger.ZERO);
    }
}
