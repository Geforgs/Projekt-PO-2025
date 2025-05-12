package po25;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import po25.commands.*;

import java.util.concurrent.Callable;

@Command(name = "dccp",
        mixinStandardHelpOptions = true,
        version = "DCCP CLI 1.0",
        description = "Desktop client for programming contest platforms.",
        subcommands = {
                ListContestsCommand.class,
                ViewContestCommand.class,
                ListTasksCommand.class,
                ViewTaskCommand.class,
                SubmitCommand.class,
                StatusCommand.class,
                HistoryCommand.class
        })

public class Main implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        System.out.println("Welcome to Desktop Client for Competitive Programming Platforms!");
        System.out.println("Use 'dccp <command> --help' for more information on a specific command.");
//         new CommandLine(this).usage(System.out);
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main())
                .setExecutionStrategy(new CommandLine.RunLast())
                .execute(args);
        System.exit(exitCode);
    }
}