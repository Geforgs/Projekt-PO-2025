package po25.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import po25.commands.mixins.PlatformOptionMixin;
import po25.service.PlatformService;

import java.util.concurrent.Callable;

@Command(name = "view-task",
        description = "Views a specific task in contest and optionally saves it.",
        mixinStandardHelpOptions = true)
public class ViewTaskCommand implements Callable<Integer> {

    @CommandLine.Mixin
    private PlatformOptionMixin platform;

    @Parameters(index = "0", description = "The contest ID.")
    private String contestId;

    @Parameters(index = "1", description = "The task ID.")
    private String taskId;

    @CommandLine.Option(names = {"-s", "--save"}, description = "Save the task content locally.")
    private boolean save;

    private PlatformService platformService;

    public ViewTaskCommand() {
        this.platformService = new PlatformService();
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("Executing view-task command...");
        System.out.println("Platform: " + platform);
        System.out.println("Task ID: " + taskId);
        if (save) {
            System.out.println("Task will be saved locally.");
            // platformService.viewAndSaveTask(platform, taskId);
        } else {
            platformService.viewTask(platform.platform, contestId, taskId);
            System.out.println("Task content would be displayed here.");
        }
        System.out.println("[Mock] Task " + taskId + " content from " + platform + " would be shown.");
        return 0;
    }
}


