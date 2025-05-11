package po25;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import po25.commands.ListTasksCommand;
//import po25.commands.ViewTaskCommand;
//import po25.commands.SubmitCommand;
//import po25.commands.StatusCommand;
//import po25.commands.HistoryCommand;

import java.util.concurrent.Callable;

@Command(name = "dccp",
        mixinStandardHelpOptions = true,
        version = "DCCP CLI 1.0",
        description = "Desktop client for programming contest platforms.",
        subcommands = {
                po25.commands.ListTasksCommand.class,
//                po25.commands.ViewTaskCommand.class,
//                po25.commands.SubmitCommand.class,
//                StatusCommand.class,    // Register new commands here
//                HistoryCommand.class
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

/// / Example Subcommand: View Task
//@Command(name = "view-task",
//        description = "Views a specific task and optionally saves it.",
//        mixinStandardHelpOptions = true)
//class ViewTaskCommand implements Callable<Integer> {
//
//    @Parameters(index = "0", description = "The platform (e.g., codeforces, satori).")
//    private String platform;
//
//    @Parameters(index = "1", description = "The task ID.")
//    private String taskId;
//
//    @Option(names = {"-s", "--save"}, description = "Save the task content locally.")
//    private boolean save;
//
//    @Override
//    public Integer call() throws Exception {
//        System.out.println("Executing view-task command...");
//        System.out.println("Platform: " + platform);
//        System.out.println("Task ID: " + taskId);
//        if (save) {
//            System.out.println("Task will be saved locally.");
//            // platformService.viewAndSaveTask(platform, taskId);
//        } else {
//            // platformService.viewTask(platform, taskId);
//            System.out.println("Task content would be displayed here.");
//        }
//        System.out.println("[Mock] Task " + taskId + " content from " + platform + " would be shown.");
//        return 0;
//    }
//}
//
//
/// / Example Subcommand: Submit Solution
//@Command(name = "submit",
//        description = "Submits a solution to a task on a specified platform.",
//        mixinStandardHelpOptions = true)
//class SubmitCommand implements Callable<Integer> {
//
//    @Parameters(index = "0", description = "The platform (e.g., codeforces, satori).")
//    private String platform;
//
//    @Parameters(index = "1", description = "The task ID or contest problem letter.")
//    private String taskId;
//
//    @Parameters(index = "2", description = "Path to the solution file.")
//    private File solutionFile; // PicoCLI can convert to File type directly
//
//    @Option(names = {"-l", "--language"}, description = "Programming language of the solution (optional, might be auto-detected).")
//    private String language;
//
//    // private PatformService platformService = new PatformService(); // Or inject it
//
//    @Override
//    public Integer call() throws Exception {
//        System.out.println("Executing submit command...");
//        if (!solutionFile.exists() || !solutionFile.isFile()) {
//            System.err.println("Error: Solution file not found or is not a regular file: " + solutionFile.getAbsolutePath());
//            return 1; // Exit code 1 for error
//        }
//
//        System.out.println("Platform: " + platform);
//        System.out.println("Task ID: " + taskId);
//        System.out.println("Solution File: " + solutionFile.getAbsolutePath());
//        if (language != null) {
//            System.out.println("Language: " + language);
//        }
//        // Implement actual submission logic here
//        // platformService.submitSolution(platform, taskId, solutionFile, language);
//        System.out.println("[Mock] Solution " + solutionFile.getName() + " for task " + taskId + " on " + platform + " submitted.");
//        return 0;
//    }
//}
//
/// / PicoCLI's built-in HelpCommand can be used by default with mixinStandardHelpOptions = true
/// / or you can define a custom one if needed.
/// / This is just an example of how to make it explicit if you prefer.
//@Command(name = "help",
//        description = "Displays help information about the available commands.",
//        mixinStandardHelpOptions = true) // Provides --help for itself
//class HelpCommand implements Callable<Integer> {
//    @Override
//    public Integer call() {
//        // The main command's help will be shown due to how execute is called if 'help' is the arg
//        // Or, you can manually build and print usage for the main command:
//        new CommandLine(new Main()).usage(System.out);
//        return 0;
//    }
//}