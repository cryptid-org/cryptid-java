package cryptid.cli.commands;

import cryptid.ibe.domain.CipherTextTuple;
import cryptid.ibe.IbeClient;
import cryptid.ibe.IbeComponentFactory;
import cryptid.ibe.domain.PublicParameters;
import cryptid.ibe.bonehfranklin.BonehFranklinIbeComponentFactoryImpl;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.PrintStream;
import java.security.SecureRandom;

import static cryptid.cli.Application.FAILURE;
import static cryptid.cli.util.CommandUtils.printCipherTextTuple;
import static cryptid.cli.util.CommandUtils.readFileIntoString;
import static cryptid.cli.util.CommandUtils.readPublicParameters;

@Command(name = "encrypt", description = "Encrypts the specified message with the specified identity.", mixinStandardHelpOptions = true)
public class EncryptCommand implements Runnable {
    @Option(names = { "-pp", "--publicParameters" },
            required = true,
            description = "The file to containing the public parameters."
    )
    private File publicParametersFile;

    @Option(names = { "-m", "--message" },
            required = true,
            description = "The file containing the message."
    )
    private File messageFile;

    @Option(names = { "-i", "--identity" },
            required = true,
            description = "The file containing the identity."
    )
    private File identityFile;

    @Option(names = { "-c", "--ciphertext" },
            required = true,
            description = "The file to write the ciphertext into."
    )
    private File ciphertextFile;

    @Override
    public void run() {
        try {
            final String message = readFileIntoString(messageFile);
            final String identity = readFileIntoString(identityFile);

            final PublicParameters publicParameters = readPublicParameters(publicParametersFile);

            final SecureRandom secureRandom = SecureRandom.getInstanceStrong();

            final IbeComponentFactory componentFactory = new BonehFranklinIbeComponentFactoryImpl(secureRandom);
            final IbeClient client = componentFactory.obtainClient(publicParameters);

            CipherTextTuple cipherTextTuple = client.encrypt(message, identity);

            try (PrintStream stream = new PrintStream(ciphertextFile)) {
                printCipherTextTuple(cipherTextTuple, stream);
            }
        } catch(Exception ex) {
            System.err.println("Could not encrypt the specified message.");
            System.err.println(ex.getMessage());

            System.exit(FAILURE);
        }
    }
}
