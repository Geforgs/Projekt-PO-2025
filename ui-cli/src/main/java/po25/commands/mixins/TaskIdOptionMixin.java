package po25.commands.mixins;

import picocli.CommandLine.Option;

public class TaskIdOptionMixin {

    @Option(names = {"-t", "--task"},
            required = true,
            description = "The ID/index of the task (e.g., 'A', 'B', '101A').")
    public String taskId;
}