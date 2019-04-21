package cryptid.ibe.domain;

/**
 * Enumeration of the security levels that can be used in the IBE.
 */
public enum SecurityLevel {
    LOWEST(160, 512, "SHA-1"),
    LOW(224, 1024, "SHA-224"),
    MEDIUM(256, 1536, "SHA-256"),
    HIGH(384, 3840, "SHA-384"),
    HIGHEST(512, 7680, "SHA-512");

    SecurityLevel(int qLength, int pLength, String hashFunction) {
        this.qLength = qLength;
        this.pLength = pLength;
        this.hashFunction = hashFunction;
    }

    private final int qLength;
    private final int pLength;
    private final String hashFunction;

    public int getqLength() {
        return qLength;
    }

    public int getpLength() {
        return pLength;
    }

    public String getHashFunction() {
        return hashFunction;
    }
}
