package po25.commands.mixins;

import picocli.CommandLine.Option;

public class ContestIdOptionMixin {

    @Option(names = {"-c", "--contest"},
            required = true,
            description = "The ID of the contest.")
    public String contestId;
}