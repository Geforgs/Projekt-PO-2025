package po25.commands.mixins;

import picocli.CommandLine.Option;

public class PlatformOptionMixin {

    @Option(names = {"-p", "--platform"},
            required = true,
            description = "The platform name (e.g., codeforces, satori).")
    public String platform;
}