package cryptid.cli;

import cryptid.cli.commands.DecryptCommand;
import cryptid.cli.commands.EncryptCommand;
import cryptid.cli.commands.ExtractCommand;
import cryptid.cli.commands.SetupCommand;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "cryptid-cli", mixinStandardHelpOptions = true, version = "CryptID.java CLI version 1.0",
        subcommands = {DecryptCommand.class, EncryptCommand.class, ExtractCommand.class, SetupCommand.class })
public final class Application implements Runnable {
    private static final CommandLine cmd = new CommandLine(new Application());

    public static final int FAILURE = 1;

    public static void main(String[] args) {
        cmd.parseWithHandler(new CommandLine.RunLast(), args);
    }

    @Override
    public void run() {
        cmd.usage(System.out);
    }
}
