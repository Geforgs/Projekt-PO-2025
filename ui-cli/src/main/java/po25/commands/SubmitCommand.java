package po25.commands;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

// TODO:
@Command(name = "submit",
        description = "Submits a solution to a task on a specified platform.",
        mixinStandardHelpOptions = true)
public class SubmitCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
