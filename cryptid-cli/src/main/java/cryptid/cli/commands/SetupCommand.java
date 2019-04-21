package cryptid.cli.commands;

import cryptid.ellipticcurve.point.affine.generator.GenerationStrategyFactory;
import cryptid.ellipticcurve.point.affine.generator.Mod3GenerationStrategy;
import cryptid.ibe.IbeInitializer;
import cryptid.ibe.domain.IbeSetup;
import cryptid.ibe.domain.SecurityLevel;
import cryptid.ibe.util.SolinasPrimeFactory;
import cryptid.ibe.bonehfranklin.BonehFranklinIbeInitializer;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.PrintStream;
import java.security.SecureRandom;

import static cryptid.cli.Application.FAILURE;
import static cryptid.cli.util.CommandUtils.printMasterSecret;
import static cryptid.cli.util.CommandUtils.printPublicParameters;

@Command(name = "setup", description = "Initializes a new IBE system instance.",  mixinStandardHelpOptions = true)
public class SetupCommand implements Runnable {
    @Option(names = { "-s", "--securityLevel" },
            required = true,
            description = "The desired level of security: ${COMPLETION-CANDIDATES}"
    )
    private SecurityLevel securityLevel;

    @Option(names = { "-pp", "--publicParameters" },
            required = true,
            description = "The file to write the public parameters into."
    )
    private File publicParametersFile;

    @Option(names = { "-ms", "--masterSecret" },
            required = true,
            description = "The file to write the master secret into."
    )
    private File masterSecretFile;

    @Override
    public void run() {
        try {
            final SecureRandom secureRandom = SecureRandom.getInstanceStrong();

            final SolinasPrimeFactory solinasPrimeFactory = new SolinasPrimeFactory(secureRandom);
            final GenerationStrategyFactory<Mod3GenerationStrategy> generationStrategyFactory =
                    curve -> new Mod3GenerationStrategy(curve, secureRandom);

            final IbeInitializer initializer = new BonehFranklinIbeInitializer(secureRandom, solinasPrimeFactory, generationStrategyFactory);

            final IbeSetup setup = initializer.setup(securityLevel);

            try (PrintStream stream = new PrintStream(publicParametersFile)) {
                printPublicParameters(setup.getPublicParameters(), stream);
            }

            try (PrintStream stream = new PrintStream(masterSecretFile)) {
                printMasterSecret(setup.getMasterSecret(), stream);
            }
        } catch (Exception ex) {
            System.err.println("Could not setup the IBE system instance.");
            System.err.print(ex.getMessage());

            System.exit(FAILURE);
        }
    }
}
