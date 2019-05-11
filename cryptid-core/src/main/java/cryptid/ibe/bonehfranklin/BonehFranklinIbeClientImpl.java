package cryptid.ibe.bonehfranklin;

import cryptid.complex.Complex;
import cryptid.ellipticcurve.point.affine.AffinePoint;
import cryptid.ibe.*;
import cryptid.ellipticcurve.pairing.tate.TatePairing;
import cryptid.ibe.domain.CipherTextTuple;
import cryptid.ibe.domain.PrivateKey;
import cryptid.ibe.domain.PublicParameters;
import cryptid.ibe.util.CanonicalUtils;
import cryptid.ibe.util.HashUtils;
import cryptid.util.MessageDigestFactory;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;

/**
 * Boneh-Franklin (RFC 5091) implementation of {@link IbeClient}.
 */
final class BonehFranklinIbeClientImpl extends IbeClient {
    private static final String EMPTY_STRING = "";

    private final SecureRandom secureRandom;
    private final MessageDigestFactory messageDigestFactory;
    private final TatePairing tatePairing;

    /**
     * Constructs a new instance.
     * @param publicParameters the public parameters of the IBE setup
     * @param secureRandom a cryptographically strong random source
     * @param messageDigestFactory an appropriate message digest source
     * @param tatePairing a Tate-pairing instance
     */
    BonehFranklinIbeClientImpl(final PublicParameters publicParameters, final SecureRandom secureRandom,
                               final MessageDigestFactory messageDigestFactory, final TatePairing tatePairing) {
        super(publicParameters);

        this.secureRandom = secureRandom;
        this.messageDigestFactory = messageDigestFactory;
        this.tatePairing = tatePairing;
    }

    @Override
    public CipherTextTuple encrypt(final String message, final String identity) {
        checkEncryptArguments(message, identity);

        byte[] messageBytes = message.getBytes();

        // Let hashlen be the length of the output of the cryptographic hash function hashfcn from the public parameters.
        final MessageDigest messageDigest = messageDigestFactory.obtainInstance();
        int hashLen = messageDigest.getDigestLength();

        // Q_id = HashToPoint(E, p, q, id, hashfcn), using Algorithm 4.4.1 (HashToPoint), which results in a point of
        // order q in E(F_p).
        AffinePoint pointQId = HashUtils.hashToPoint(publicParameters.getEllipticCurve(),
                publicParameters.getEllipticCurve().getFieldOrder(), publicParameters.getQ(), identity, messageDigest);

        // Select a random hashlen-bit vector rho, represented as (hashlen / 8)-octet string in big-endian convention
        // I think the comment above is wrong.
        byte[] rho = new byte[hashLen];
        secureRandom.nextBytes(rho);

        // Let t = hashfcn(m), a hashlen-octet string resulting from applying the hashfcn algorithm to the input m
        messageDigest.update(messageBytes);
        byte[] t = messageDigest.digest();

        // Let l = HashToRange(rho || t, q, hashfcn), an integer in the range 0 to q - 1 resulting from applying
        // Algorithm 4.1.1 (HashToRange) to the (2 * hashlen)-octet concatenation of rho and t
        byte[] concat = new byte[rho.length + t.length];
        System.arraycopy(rho, 0, concat, 0, rho.length);
        System.arraycopy(t, 0, concat, rho.length, t.length);
        BigInteger l = HashUtils.hashToRange(concat, publicParameters.getQ(), messageDigest);

        // Let U = [l]P, which is a point of order q in E(F_p)
        AffinePoint cipherPointU = publicParameters.getPointP().multiply(l, publicParameters.getEllipticCurve());

        // Let theta = Pairing(E, p, q, P_pub, Q_id), which is an element of the extension field F_p^2 obtained using
        // the modified Tate pairing of Algorithm 4.5.1 (Pairing).
        Complex theta = tatePairing.performPairing(publicParameters.getPointPpublic(), pointQId);

        // Let theta' = theta^l, which is theta raised to the power of l in F_p^2
        Complex thetaPrime = theta.modPow(l, publicParameters.getEllipticCurve().getFieldOrder());

        // Let z = Canonical(p, k, 0, theta'), using Algorithm 4.3.1 (Canonical), the result of which is a canonical
        // string representation of theta'
        byte[] z = CanonicalUtils.canonical(publicParameters.getEllipticCurve().getFieldOrder(), CanonicalUtils.CanonicalOrdering.IMAGINARY_FIRST, thetaPrime);

        // Let w = hashfcn(z) using the hashfcn hashing algorithm, the result of which is a hashlen-octet string
        messageDigest.update(z);
        byte[] w = messageDigest.digest();

        // Let V = w XOR rho, which is the hashlen-octet long bit-wise XOR of w and rho
        byte[] cipherV = new byte[hashLen];
        for(int i = 0; i < hashLen; i++) {
            cipherV[i] = (byte)(w[i] ^ rho[i]);
        }

        // Let W = HashBytes(|m|, rho, hashfcn) XOR m, which is the bit-wise XOR of m with the first |m| octets of the
        // pseudo-random bytes produced by Algorithm 4.2.1 (HashBytes) with seed rho
        byte[] cipherW = new byte[messageBytes.length];
        byte[] hashBytes = HashUtils.hashBytes(messageBytes.length, rho, messageDigest);
        for(int i = 0; i < messageBytes.length; i++) {
            cipherW[i] = (byte)(hashBytes[i] ^ messageBytes[i]);
        }

        // The ciphertext is the triple (U, V, W)
        return new CipherTextTuple(cipherPointU, cipherV, cipherW);
    }

    @Override
    public Optional<String> decrypt(final PrivateKey privateKey, final CipherTextTuple ciphertext) {
        checkDecryptArguments(privateKey, ciphertext);

        // Let hashlen be the length of the output of the hash function hashlen measured in octets
        final MessageDigest messageDigest = messageDigestFactory.obtainInstance();
        int hashLen = messageDigest.getDigestLength();

        // Let theta = Pairing(E, p ,q, U, S_id) by applying the modified Tate pairing of Algorithm 4.5.1 (Pairing).
        Complex theta = tatePairing.performPairing(ciphertext.getCipherU(), privateKey.getData());

        // Let z = Canonical(p, k, 0, theta) using Algorithm 4.3.1 (Canonical), the result of which is a canonical string
        // representation of theta.
        byte[] z = CanonicalUtils.canonical(publicParameters.getEllipticCurve().getFieldOrder(), CanonicalUtils.CanonicalOrdering.IMAGINARY_FIRST, theta);

        // Let w = hashfcn(z) using the hashfcn hashing algorithm, the result of which is a hashlen-octet string
        messageDigest.update(z);
        byte[] w = messageDigest.digest();

        // Let rho = w XOR V, the bit-wise XOR of w and V
        byte[] rho = new byte[hashLen];
        for(int i = 0; i < hashLen; i++) {
            rho[i] = (byte)(w[i] ^ ciphertext.getCipherV()[i]);
        }

        // Let m = HashBytes(|W|, rho, hashfcn) XOR W, which is the bit-wise XOR of m with the first |W| octets of the
        // pseudo-random bytes produced by Algorithm 4.2.1 (HashBytes) with seed rho.
        byte[] m = new byte[ciphertext.getCipherW().length];
        byte[] hashBytes = HashUtils.hashBytes(ciphertext.getCipherW().length, rho, messageDigest);
        for(int i = 0; i < ciphertext.getCipherW().length; i++) {
            m[i] = (byte)(hashBytes[i] ^ ciphertext.getCipherW()[i]);
        }

        // Let t = hashfcn(m) using the hashfcn algorithm
        messageDigest.update(m);
        byte[] t = messageDigest.digest();

        // Let l = HashToRange(rho || t, q, hashfcn) using Algorithm 4.1.1 (HashToRange) on the (2 * hashlen)-octet
        // concatenation of rho and t.
        byte[] concat = new byte[rho.length + t.length];
        System.arraycopy(rho, 0, concat, 0, rho.length);
        System.arraycopy(t, 0, concat, rho.length, t.length);
        BigInteger l = HashUtils.hashToRange(concat, publicParameters.getQ(), messageDigest);

        // Verify that U = [l]P
        // If this is the case, then the decrypted plaintext m is returned
        if(ciphertext.getCipherU().equals(publicParameters.getPointP().multiply(l, publicParameters.getEllipticCurve()))) {
            return Optional.of(new String(m));
        }

        // Otherwise, the ciphertext is rejected and no plaintext is returned.
        return Optional.empty();
    }

    private void checkEncryptArguments(final String message, final String identity) {
        Objects.requireNonNull(message);
        Objects.requireNonNull(identity);

        if (message.equals(EMPTY_STRING)) {
            throw new IllegalArgumentException("The message must not be empty!");
        }

        if (identity.equals(EMPTY_STRING)) {
            throw new IllegalArgumentException("The identity must not be empty!");
        }
    }

    private void checkDecryptArguments(final PrivateKey privateKey, final CipherTextTuple ciphertext) {
        Objects.requireNonNull(privateKey);
        Objects.requireNonNull(ciphertext);
    }
}
