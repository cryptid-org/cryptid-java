package cryptid.ibe.domain;

import cryptid.ellipticcurve.TypeOneEllipticCurve;
import cryptid.ellipticcurve.point.affine.AffinePoint;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Immutable class holding the public parameters of an IBE setup.
 */
public final class PublicParameters {
    private final TypeOneEllipticCurve ellipticCurve;
    private final BigInteger q;
    private final AffinePoint pointP;
    private final AffinePoint pointPpublic;
    private final String hashFunction;

    public PublicParameters(TypeOneEllipticCurve ellipticCurve, BigInteger q, AffinePoint pointP, AffinePoint pointPpublic, String hashFunction) {
        this.ellipticCurve = Objects.requireNonNull(ellipticCurve);
        this.q = Objects.requireNonNull(q);
        this.pointP = Objects.requireNonNull(pointP);
        this.pointPpublic = Objects.requireNonNull(pointPpublic);
        this.hashFunction = Objects.requireNonNull(hashFunction);
    }

    public TypeOneEllipticCurve getEllipticCurve() {
        return ellipticCurve;
    }

    public BigInteger getQ() {
        return q;
    }

    public AffinePoint getPointP() {
        return pointP;
    }

    public AffinePoint getPointPpublic() {
        return pointPpublic;
    }

    public String getHashFunction() { return hashFunction; }
}
