# Up & Running – The Detailed Way

Instead of using IBE via the `CryptID` class, this document will guide you through the steps of manual initialization and usage of the IBE classes.

## Setup

In order to encrypt and decrypt messages with the Boneh-Frankline IBE system, a setup needs to be initialized. This can be done using the `BonehFranklinIbeInitializer` class which is an instance of the `IbeInitializer` interface.

The constructor of `BonehFranklinIbeInitializer` expects the following:

  * `SecureRandom` - a cryptographically strong random number generator used to generate setup data,
  * `SolinasPrimeFactory` - a source of Solinas primes,
  * `GenerationStrategyFactory<T extends AffinePointGenerationStrategy>` - Creating a new IBE setup requires a random point on the underlying elliptic curve (this is slightly imprecise, but just okay for now). A random point can be generated using an `AffinePointGenerationStrategy`. A factory is required to produce generation strategy instances for each curve. A good choice for the returned strategy is the `Mod3GenerationStrategy` class. 

Having a `BonehFranklinIbeInitializer` instance, we can now setup a new IBE configuration using the `IbeSetup setup(SecurityLevel)` method. The returned `IbeSetup` instance is an immutable object holding the public parameters and the master secret of our configuration. The latter should be kept private.

Once we have established a setup, the public parameters and the master secret can be used to create subsequent clients and private key generators as long as we need.

## Creating a PKG

The private key generator is responsible for extracting private keys for appropriate identities. 

In the Boneh-Franklin IBE system, we first need to create an instance of `BonehFranklinIbeComponentFactory`. Instantiating this class requires solely a `SecureRandom` object.

A private key generator can be obtained by calling the `PrivateKeyGenerator obtainPrivateKeyGenerator(PublicParameters, BigInteger)` method. This takes the public parameters and the master secret wrapped in the `IbeSetup` instance returned by `IbeInitializer.setup(SecurityLevel)`.

Once we have a `PrivateKeyGenerator` instance, we can use its `PrivateKey extract(String)` method to create private keys.

## Encryption and decryption

Encryption and decryption can be done using `IbeClient` instances. 

Again, when dealing with the Boneh-Franklin scheme, we first need an instance of `BonehFranklinIbeComponentFactory`. Calling the `IbeClient obtainClient(PublicParameters)` method produces an `IbeClient` for our disposal.

Note that creating a client requires **only** the public parameters of the IBE setup.

Encryption is done using the `CipherTextTuple encrypt(String, String)` method which takes the message we want to encrypt and the identity of the recipient.

Decryption can be performed by calling `Optional<String> decrypt(PrivateKey, CipherTextTuple)`. Of course, we first need to obtain an appropriate `PrivateKey` from a private key generator.


## Example

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
