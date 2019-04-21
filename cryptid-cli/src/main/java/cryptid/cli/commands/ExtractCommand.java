package cryptid.cli.commands;

import cryptid.ibe.IbeComponentFactory;
import cryptid.ibe.PrivateKeyGenerator;
import cryptid.ibe.domain.PublicParameters;
import cryptid.ibe.bonehfranklin.BonehFranklinIbeComponentFactoryImpl;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.SecureRandom;

import static cryptid.cli.Application.FAILURE;
import static cryptid.cli.util.CommandUtils.*;

@Command(name = "extract", description = "Extracts the private key corresponding to a specified identity.", mixinStandardHelpOptions = true)
public class ExtractCommand implements Runnable {
    @Option(names = { "-pp", "--publicParameters" },
            required = true,
            description = "The file to containing the public parameters."
    )
    private File publicParametersFile;

    @Option(names = { "-ms", "--masterSecret" },
            required = true,
            description = "The file containing the master secret."
    )
    private File masterSecretFile;

    @Option(names = { "-i", "--identity" },
            required = true,
            description = "The file containing the identity."
    )
    private File identityFile;

    @Option(names = { "-pk", "--privateKey" },
            required = true,
            description = "The file to write the private key into."
    )
    private File privateKeyFile;

    @Override
    public void run() {
        try {
            final BigInteger masterSecret = readMasterSecret(masterSecretFile);
            final PublicParameters publicParameters = readPublicParameters(publicParametersFile);

            final String identity = readFileIntoString(identityFile);

            final SecureRandom secureRandom = SecureRandom.getInstanceStrong();

            final IbeComponentFactory componentFactory = new BonehFranklinIbeComponentFactoryImpl(secureRandom);
            final PrivateKeyGenerator privateKeyGenerator = componentFactory.obtainPrivateKeyGenerator(publicParameters, masterSecret);

            try (PrintStream stream = new PrintStream(privateKeyFile)) {
                printPrivateKey(privateKeyGenerator.extract(identity), stream);
            }
        } catch (Exception ex) {
            System.err.println("Could not extract private key.");
            System.err.println(ex.getMessage());

            System.exit(FAILURE);
        }
    }
}
