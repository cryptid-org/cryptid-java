package cryptid.ellipticcurve;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Class representing elliptic curves of the form {@code y^2 = x^3 + 1} with {@code p} congruent to 11 modulo 12.
 */
public class TypeOneEllipticCurve extends EllipticCurve {
    private static final BigInteger ELEVEN = BigInteger.valueOf(11L);
    private static final BigInteger TWELVE = BigInteger.valueOf(12L);

    private TypeOneEllipticCurve(final BigInteger fieldOrder) {
        super(BigInteger.ZERO, BigInteger.ONE, fieldOrder);
    }

    /**
     * Creates a new Type-1 elliptic curve of the specified field order.
     * @param fieldOrder the order of the finite field
     * @throws IllegalArgumentException if the field order is not congruent to 11 modulo 12
     * @throws NullPointerException if the field order is {@code null}
     * @return a new Type-1 elliptic curve
     */
    public static TypeOneEllipticCurve ofOrder(final BigInteger fieldOrder) {
        Objects.requireNonNull(fieldOrder);

        if (!fieldOrder.mod(TWELVE).equals(ELEVEN)) {
            throw new IllegalArgumentException("The field order must be congruent to 11 modulo 12!");
        }

        return new TypeOneEllipticCurve(fieldOrder);
    }
}
