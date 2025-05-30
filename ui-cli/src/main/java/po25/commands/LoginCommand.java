package po25.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import po25.PlatformException;
import po25.commands.mixins.PlatformOptionMixin;
import po25.service.PlatformService;

import java.io.Console;
import java.util.Arrays;
import java.util.concurrent.Callable;

@Command(name = "login",
        description = "Logs you into the specified platform by prompting for credentials.",
        mixinStandardHelpOptions = true)
public class LoginCommand implements Callable<Integer> {

    @CommandLine.Mixin
    private PlatformOptionMixin platformOptionMixin;

    private PlatformService platformService;

    public LoginCommand() {
        this.platformService = new PlatformService();
    }

    @Override
    public Integer call() {
        String platformName = platformOptionMixin.platform;
        String usernameInput;
        char[] passwordInput = null;

        Console console = System.console();
        if (console == null) {
            System.err.println("Error: Cannot access console to read username and password securely.");
            System.err.println("This command requires an interactive console environment.");
            return 1;
        }

        try {
            usernameInput = console.readLine("Enter username for %s: ", platformName);
            if (usernameInput == null || usernameInput.trim().isEmpty()) {
                System.err.println("Username cannot be empty.");
                return 1;
            }
            usernameInput = usernameInput.trim();

            passwordInput = console.readPassword("Enter password for %s: ", usernameInput);
            if (passwordInput == null || passwordInput.length == 0) {
                System.err.println("Password cannot be empty.");
                return 1;
            }

            System.out.println("Attempting to log into " + platformName + " as " + usernameInput + "...");
            platformService.login(platformName, usernameInput, passwordInput);
            System.out.println("Successfully logged into " + platformName + " as " + usernameInput + "!");
            return 0;
        } catch (PlatformException e) {
            System.err.println("Login failed: " + e.getMessage());
            // e.printStackTrace();
            return 1;
        } finally {
            if (passwordInput != null) {
                Arrays.fill(passwordInput, ' ');
            }
        }
    }
}