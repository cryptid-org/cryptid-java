package cryptid.ibe.domain;

import cryptid.ellipticcurve.point.affine.AffinePoint;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents the ciphertext which is the result of the IBE encryption.
 */
public class CipherTextTuple {
    private final AffinePoint cipherU;
    private final byte[] cipherV;
    private final byte[] cipherW;

    public CipherTextTuple(AffinePoint cipherU, byte[] cipherV, byte[] cipherW) {
        this.cipherU = Objects.requireNonNull(cipherU);
        this.cipherV = Objects.requireNonNull(cipherV);
        this.cipherW = Objects.requireNonNull(cipherW);
    }

    public AffinePoint getCipherU() {
        return cipherU;
    }

    public byte[] getCipherV() {
        return cipherV;
    }

    public byte[] getCipherW() {
        return cipherW;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CipherTextTuple that = (CipherTextTuple) o;

        if (!cipherU.equals(that.cipherU)) return false;
        if (!Arrays.equals(cipherV, that.cipherV)) return false;
        return Arrays.equals(cipherW, that.cipherW);
    }

    @Override
    public int hashCode() {
        int result = cipherU.hashCode();
        result = 31 * result + Arrays.hashCode(cipherV);
        result = 31 * result + Arrays.hashCode(cipherW);
        return result;
    }

    @Override
    public String toString() {
        return "CipherTextTuple{" +
                "cipherU=" + cipherU +
                ", cipherV=" + Arrays.toString(cipherV) +
                ", cipherW=" + Arrays.toString(cipherW) +
                '}';
    }
}
