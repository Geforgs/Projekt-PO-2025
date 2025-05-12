package po25.commands;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

// TODO:
@Command(name = "history",
        description = "Displays submission history for contest and task.",
        mixinStandardHelpOptions = true)
public class HistoryCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        return 0;
    }
}