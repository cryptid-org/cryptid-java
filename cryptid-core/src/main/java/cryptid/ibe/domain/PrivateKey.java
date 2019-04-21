package cryptid.ibe.domain;

import cryptid.ellipticcurve.point.affine.AffinePoint;

import java.util.Objects;

/**
 * Immutable class wrapping private key data.
 */
public final class PrivateKey {
    private final AffinePoint data;

    /**
     * Constructs a new private key using the specified data.
     * @param data the private key data
     */
    public PrivateKey(AffinePoint data) {
        this.data = Objects.requireNonNull(data);
    }

    public AffinePoint getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateKey that = (PrivateKey) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
