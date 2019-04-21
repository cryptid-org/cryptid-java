package cryptid.cli.commands;

import cryptid.ibe.domain.CipherTextTuple;
import cryptid.ibe.IbeClient;
import cryptid.ibe.IbeComponentFactory;
import cryptid.ibe.domain.PrivateKey;
import cryptid.ibe.domain.PublicParameters;
import cryptid.ibe.bonehfranklin.BonehFranklinIbeComponentFactoryImpl;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.PrintStream;
import java.security.SecureRandom;
import java.util.Optional;

import static cryptid.cli.Application.FAILURE;
import static cryptid.cli.util.CommandUtils.*;

@Command(name = "decrypt", description = "Decrypts the specified ciphertext with the specified private key.", mixinStandardHelpOptions = true)
public class DecryptCommand implements Runnable {
    @Option(names = { "-pp", "--publicParameters" },
            required = true,
            description = "The file to containing the public parameters."
    )
    private File publicParametersFile;

    @Option(names = { "-m", "--message" },
            required = true,
            description = "The file to write the message into."
    )
    private File messageFile;

    @Option(names = { "-c", "--ciphertext" },
            required = true,
            description = "The file containing the ciphertext."
    )
    private File ciphertextFile;

    @Option(names = { "-pk", "--privateKey" },
            required = true,
            description = "The file to containing the private key."
    )
    private File privateKeyFile;

    @Override
    public void run() {
        try {
            final PrivateKey privateKey = readPrivateKey(privateKeyFile);
            final CipherTextTuple cipherTextTuple = readCipherTextTuple(ciphertextFile);
            final PublicParameters publicParameters = readPublicParameters(publicParametersFile);

            final SecureRandom secureRandom = SecureRandom.getInstanceStrong();

            final IbeComponentFactory componentFactory = new BonehFranklinIbeComponentFactoryImpl(secureRandom);
            final IbeClient client = componentFactory.obtainClient(publicParameters);

            Optional<String> messageOptional = client.decrypt(privateKey, cipherTextTuple);

            if (messageOptional.isPresent()) {
                try (PrintStream stream = new PrintStream(messageFile)) {
                    stream.print(messageOptional.get());
                }
            } else {
                throw new Exception("Could not decrypt the ciphertext with the specified private key.");
            }
        } catch(Exception ex) {
            System.err.println("Could not decrypt the specified ciphertext.");
            System.err.println(ex.getMessage());

            System.exit(FAILURE);
        }
    }
}
