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

[![CircleCI](https://circleci.com/gh/battila7/cryptid-java.svg?style=svg&circle-token=53e7cd3c82351e414663d74fe8617f31218d64e3)](https://circleci.com/gh/battila7/cryptid-java)

Java 8 library implementing the Boneh-Franklin IBE system as described in [RFC 5091](https://tools.ietf.org/html/rfc5091).

CryptID.java can be used to setup Boneh-Frankling Identity-based Encryption system instances of various security levels. Clients of the library can

  * encrypt messages with arbitrary identities,
  * extract private keys corresponding to appropriate identities
  * and decrypt messages with the extracted private keys.

For more information on Identity-based Encryption, please read the aforementioned RFC or some related books and papers.

## Install



## Working with CryptID.java

In the following, the usage of the `cryptid-core` module is described. `cryptid-cli` is a standalone console application built upon `cryptid-core`. For its usage, please see the [cryptid-cli/README.md](cryptid-cli/README.md) file.

### Up & Running – The Quick Way

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

The `CryptID.setupBonehFranklin` method uses defaults when setting up the IBE system. For a more customizable usage, please see the following section.

### Up & Running – The Hard and Detailed Way

#### Setup

In order to encrypt and decrypt messages with the Boneh-Frankline IBE system, a setup needs to be initialized. This can be done using the `BonehFranklinIbeInitializer` class which is an instance of the `IbeInitializer` interface.

The constructor of `BonehFranklinIbeInitializer` expects the following:

  * `SecureRandom` - a cryptographically strong random number generator used to generate setup data,
  * `SolinasPrimeFactory` - a source of Solinas primes,
  * `GenerationStrategyFactory<T extends AffinePointGenerationStrategy>` - Creating a new IBE setup requires a random point on the underlying elliptic curve (this is slightly imprecise, but just okay for now). A random point can be generated using an `AffinePointGenerationStrategy`. A factory is required to produce generation strategy instances for each curve. A good choice for the returned strategy is the `Mod3GenerationStrategy` class. 

Having a `BonehFranklinIbeInitializer` instance, we can now setup a new IBE configuration using the `IbeSetup setup(SecurityLevel)` method. The returned `IbeSetup` instance is an immutable object holding the public parameters and the master secret of our configuration. The latter should be kept private.

Once we have established a setup, the public parameters and the master secret can be used to create subsequent clients and private key generators as long as we need.

#### Creating a PKG

The private key generator is responsible for extracting private keys for appropriate identities. 

In the Boneh-Franklin IBE system, we first need to create an instance of `BonehFranklinIbeComponentFactory`. Instantiating this class requires solely a `SecureRandom` object.

A private key generator can be obtained by calling the `PrivateKeyGenerator obtainPrivateKeyGenerator(PublicParameters, BigInteger)` method. This takes the public parameters and the master secret wrapped in the `IbeSetup` instance returned by `IbeInitializer.setup(SecurityLevel)`.

Once we have a `PrivateKeyGenerator` instance, we can use its `PrivateKey extract(String)` method to create private keys.

#### Encryption and decryption

Encryption and decryption can be done using `IbeClient` instances. 

Again, when dealing with the Boneh-Franklin scheme, we first need an instance of `BonehFranklinIbeComponentFactory`. Calling the `IbeClient obtainClient(PublicParameters)` method produces an `IbeClient` for our disposal.

Note that creating a client requires **only** the public parameters of the IBE setup.

Encryption is done using the `CipherTextTuple encrypt(String, String)` method which takes the message we want to encrypt and the identity of the recipient.

Decryption can be performed by calling `Optional<String> decrypt(PrivateKey, CipherTextTuple)`. Of course, we first need to obtain an appropriate `PrivateKey` from a private key generator.


### Example

Here's a fully functional example that demonstrates the advanced usage of CryptID.java:

~~~~Java
public class App {
    public static void main(String[] args) throws Exception {
        // Setup some prerequisites
        SecureRandom secureRandom = SecureRandom.getInstanceStrong();
        SolinasPrimeFactory solinasPrimeFactory = new SolinasPrimeFactory(secureRandom);
        GenerationStrategyFactory<Mod3GenerationStrategy> generationStrategyFactory =
                ellipticCurve -> new Mod3GenerationStrategy(ellipticCurve, secureRandom);

        // Create a new initializer that can create IBE setups
        IbeInitializer initializer = new BonehFranklinIbeInitializer(secureRandom, solinasPrimeFactory, generationStrategyFactory);

        // Establish a new IBE setup with the desired security level.
        // Setup is called only once. Afterwards, the created setup can be used for subsequent actions.
        IbeSetup setup = initializer.setup(SecurityLevel.LOWEST);

        // Create a component factory
        // A component factory is needed for encryption, decryption and private key extraction.
        IbeComponentFactory componentFactory = new BonehFranklinIbeComponentFactoryImpl(secureRandom);

        // Obtain a client and a private key generator using the IBE setup
        // The client can perform encryption and decryption. Creating a client only requires the public parameters.
        IbeClient client = componentFactory.obtainClient(setup.getPublicParameters());
        // The PKG can extract private keys corresponding to various identities. In order to create a PKG, you
        // MUST hold the master secret.
        PrivateKeyGenerator privateKeyGenerator = componentFactory.obtainPrivateKeyGenerator(setup.getPublicParameters(), setup.getMasterSecret());

        // Ready to roll!
        String message = "Ironic.";
        String identity = "darth.plagueis@sith.com";

        // Encrypt the message
        CipherTextTuple cipherText = client.encrypt(message, identity);

        // Obtain the private key corresponding to the identity
        PrivateKey privateKey = privateKeyGenerator.extract(identity);

        // Decrypt the message
        client.decrypt(privateKey, cipherText)
                .ifPresent(System.out::println);
    }
}
~~~~

## Building CryptID.java

### Using task.js

CryptID.java can be built using `task.js` from the repository root:

~~~~bash
./task.js cryptid-java build
~~~~

Tests can be skipped by passing the `--skipTests` option to `task.js`.

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
./mvnw site
~~~~

command.

## Runtime dependencies

`cryptid-core` has **no** runtime dependencies apart from the Java 8 Standard Library.

However, `cryptid-core` and `cryptid-cli` can only be used in an environment which provides

  * a strong `SecureRandom` implementation (according to the docs, every implementation of the Java platform includes a strong implementation),
  * an appropriate message digest algorithm:
    * SHA-1 for `SecurityLevel.LOWEST` (every implementation of the Java platform includes SHA-1)
    * SHA-224 for `SecurityLevel.LOW`
    * SHA-256 for `SecurityLevel.MEDIUM` (every implementation of the Java platform includes SHA-256)
    * SHA-384 for `SecurityLevel.HIGH`
    * SHA-512 for `SecurityLevel.HIGHEST`
