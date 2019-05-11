<p align="center">
  <a href="https://github.com/cryptid-org">
    <img alt="CryptID" src="docs/img/cryptid-logo.png" width="200">
  </a>
</p>

<p align="center">
Cross-platform Identity-based Encryption solution.
</p>

---

# CryptID.java

Java 8 library implementing the Boneh-Franklin IBE system as described in [RFC 5091](https://tools.ietf.org/html/rfc5091).

If you're new to CryptID and Identity-based Encryption, then make sure to check out the [CryptID Getting Started](https://github.com/cryptid-org/getting-started) guide.

## Install

Currently, CryptID.java is not available in Maven Central, thus the easiest way to install it is as follows:

  1. Clone this repository.
  2. Issue `./mvnw install` in the repository root. This will install CryptID.java to your local Maven repository.
  3. Add the following dependency to your POM:

     ~~~~XML
     <dependency>
       <groupId>cryptid</groupId>
       <artifactId>cryptid-core</artifactId>
       <version>1.0.0</version>
     </dependency>
     ~~~~

## Up & Running

Setting up and utilizing IBE can be realized in a few lines using the `CryptID` class:

~~~~Java
public class App {
    public static void main(String[] args) throws Exception {
        // Setup a Boneh-Franklin IBE system of the specified security level.
        // Here we use LOWEST to make things fast.
        IdentityBasedEncryption ibe = CryptID.setupBonehFranklin(SecurityLevel.LOWEST);

        String message = "Ironic.";
        String identity = "darth.plagueis@sith.com";

        // Encrypt the message
        CipherTextTuple cipherText = ibe.encrypt(message, identity);

        // Obtain the private key corresponding to the identity
        PrivateKey privateKey = ibe.extract(identity);

        // Decrypt the message
        ibe.decrypt(privateKey, cipherText)
                .ifPresent(System.out::println);
    }
}
~~~~

The `CryptID.setupBonehFranklin` method uses defaults when setting up the IBE system. In addition, it cannot be used to create only selected components of the IBE system.

For a more customizable usage, please see [Advanced Usage](docs/advanced-usage.md).

## Building CryptID.java

### Manual build

CryptID.java can be built using Maven (or the provided Maven wrapper):

~~~~bash
./mvnw package
~~~~

The tests are written in Groovy-Spock and can be run using the

~~~~bash
./mvnw test
~~~~

command.

The Javadoc documentation can be created using the

~~~~bash
./mvnw javadoc:javadoc
~~~~

command.

## Dependencies

`cryptid-core` has **no** runtime dependencies apart from the Java 8 Standard Library.

However, `cryptid-core` and `cryptid-cli` can only be used in an environment which provides

  * a strong `SecureRandom` implementation (according to the docs, every implementation of the Java platform includes a strong implementation),
  * an appropriate message digest algorithm:
    * SHA-1 for `SecurityLevel.LOWEST` (every implementation of the Java platform includes SHA-1)
    * SHA-224 for `SecurityLevel.LOW`
    * SHA-256 for `SecurityLevel.MEDIUM` (every implementation of the Java platform includes SHA-256)
    * SHA-384 for `SecurityLevel.HIGH`
    * SHA-512 for `SecurityLevel.HIGHEST`

## License

CryptID.java is licensed under the [Apache License 2.0](LICENSE).

## Acknowledgements

This work is supported by the construction EFOP-3.6.3-VEKOP-16-2017-00002. The project is supported by the European Union, co-financed by the European Social Fund.

<p align="right">
  <img alt="CryptID" src="docs/img/szechenyi-logo.jpg" width="350">
</p>
