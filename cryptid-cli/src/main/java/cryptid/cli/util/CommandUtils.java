package cryptid.cli.util;

import cryptid.ellipticcurve.TypeOneEllipticCurve;
import cryptid.ellipticcurve.point.affine.AffinePoint;
import cryptid.ibe.domain.CipherTextTuple;
import cryptid.ibe.domain.PrivateKey;
import cryptid.ibe.domain.PublicParameters;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Scanner;

public final class CommandUtils {
    private static final int INPUT_RADIX = 10;

    /*
     * Thread-safe (see javadoc).
     */
    private static final Base64.Encoder base64Encoder = Base64.getEncoder();

    /*
     * Thread-safe (see javadoc).
     */
    private static final Base64.Decoder base64Decoder = Base64.getDecoder();

    private CommandUtils() {
        // Cannot be constructed.
    }

    public static String readFileIntoString(final File file) throws IOException {
        final byte[] contents = Files.readAllBytes(file.toPath());

        return new String(contents, Charset.defaultCharset());
    }

    public static void printPublicParameters(PublicParameters publicParameters, PrintStream stream) {
        stream.println(publicParameters.getHashFunction());
        stream.println(publicParameters.getEllipticCurve().getFieldOrder());
        stream.println(publicParameters.getQ());
        printAffine(publicParameters.getPointP(), stream);
        printAffine(publicParameters.getPointPpublic(), stream);
    }

    public static PublicParameters readPublicParameters(File file) throws DeserializationException {
        try {
            final Scanner scanner = new Scanner(file);

            final String hashFunction = scanner.nextLine();
            final TypeOneEllipticCurve ellipticCurve = TypeOneEllipticCurve.ofOrder(new BigInteger(scanner.nextLine(), INPUT_RADIX));
            final BigInteger q = new BigInteger(scanner.nextLine(), INPUT_RADIX);
            final AffinePoint pointP = readAffine(scanner.nextLine());
            final AffinePoint pointPpublic = readAffine(scanner.nextLine());

            PublicParameters publicParameters = new PublicParameters(ellipticCurve, q, pointP, pointPpublic, hashFunction);

            scanner.close();

            return publicParameters;
        } catch (Exception ex) {
            throw new DeserializationException(ex);
        }
    }

    public static void printPrivateKey(PrivateKey privateKey, PrintStream stream) {
        printAffine(privateKey.getData(), stream);
    }

    public static PrivateKey readPrivateKey(File file) throws DeserializationException {
        try {
            final Scanner scanner = new Scanner(file);

            final AffinePoint point = readAffine(scanner.nextLine());

            PrivateKey privateKey = new PrivateKey(point);

            scanner.close();

            return privateKey;
        } catch (Exception ex) {
            throw new DeserializationException(ex);
        }
    }

    public static void printCipherTextTuple(CipherTextTuple cipherTextTuple, PrintStream stream) {
        printAffine(cipherTextTuple.getCipherU(), stream);
        stream.println(base64Encoder.encodeToString(cipherTextTuple.getCipherV()));
        stream.println(base64Encoder.encodeToString(cipherTextTuple.getCipherW()));
    }

    public static CipherTextTuple readCipherTextTuple(File file) throws DeserializationException {
        try {
            final Scanner scanner = new Scanner(file);

            final AffinePoint cipherU = readAffine(scanner.nextLine());
            final byte[] cipherV = base64Decoder.decode(scanner.nextLine());
            final byte[] cipherW = base64Decoder.decode(scanner.nextLine());

            CipherTextTuple cipherTextTuple = new CipherTextTuple(cipherU, cipherV, cipherW);

            scanner.close();

            return cipherTextTuple;
        } catch (Exception ex) {
            throw new DeserializationException(ex);
        }
    }

    public static void printMasterSecret(BigInteger masterSecret, PrintStream stream) {
        stream.println(masterSecret);
    }

    public static BigInteger readMasterSecret(File file) throws DeserializationException {
        try {
            final Scanner scanner = new Scanner(file);

            final BigInteger masterSecret = new BigInteger(scanner.nextLine(), INPUT_RADIX);

            scanner.close();

            return masterSecret;
        } catch (Exception ex) {
            throw new DeserializationException(ex);
        }
    }

    public static void printAffine(AffinePoint affinePoint, PrintStream stream) {
        stream.printf("%s %s%n",
                affinePoint.getX(), affinePoint.getY());
    }

    public static AffinePoint readAffine(final String line) {
        final String[] fragments = line.split(" ");

        final BigInteger x = new BigInteger(fragments[0], INPUT_RADIX);
        final BigInteger y = new BigInteger(fragments[1], INPUT_RADIX);

        return new AffinePoint(x, y);
    }
}
