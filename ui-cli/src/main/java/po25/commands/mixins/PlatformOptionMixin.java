package po25.commands.mixins;

import picocli.CommandLine.Option;

public class PlatformOptionMixin {

    @Option(names = {"-p", "--platform"},
            required = true,
            description = "The platform name (e.g., codeforces, satori).")
    public String platform;
    // or package-private if mixins and commands are in the same package.
    // Can also use a getter method if preferred.
}