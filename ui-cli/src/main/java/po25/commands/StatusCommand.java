package po25.commands;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

// TODO:
@Command(name = "status",
        description = "Checks the status of a specific submission or lists recent submissions.",
        mixinStandardHelpOptions = true)
public class StatusCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
