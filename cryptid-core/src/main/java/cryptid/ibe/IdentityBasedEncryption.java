package cryptid.ibe;

import cryptid.ibe.domain.CipherTextTuple;
import cryptid.ibe.domain.PrivateKey;

import java.util.Objects;
import java.util.Optional;

public class IdentityBasedEncryption {
    private final IbeClient client;
    private final PrivateKeyGenerator privateKeyGenerator;

    public IdentityBasedEncryption(final IbeClient client, final PrivateKeyGenerator privateKeyGenerator) {
        this.client = Objects.requireNonNull(client);
        this.privateKeyGenerator = Objects.requireNonNull(privateKeyGenerator);
    }

    public CipherTextTuple encrypt(final String message, final String identity) {
        return client.encrypt(message, identity);
    }

    public Optional<String> decrypt(final PrivateKey privateKey, final CipherTextTuple ciphertext) {
        return client.decrypt(privateKey, ciphertext);
    }

    public PrivateKey extract(final String identity) {
        return privateKeyGenerator.extract(identity);
    }
}
