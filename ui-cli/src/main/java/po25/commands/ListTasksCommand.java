package po25.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import po25.commands.mixins.ContestIdOptionMixin;
import po25.commands.mixins.PlatformOptionMixin;
import po25.service.PlatformService;
import po25.Task;

import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "list-tasks",
        aliases = {"lt"},
        description = "Lists tasks for a specific contest on the given platform.",
        mixinStandardHelpOptions = true)
public class ListTasksCommand implements Callable<Integer> {

    @CommandLine.Mixin
    private PlatformOptionMixin platformOptionMixin;

    @CommandLine.Mixin
    private ContestIdOptionMixin contestIdOptionMixin;

    private PlatformService platformService;

    public ListTasksCommand() {
        this.platformService = new PlatformService();
    }

    @Override
    public Integer call() throws Exception {
        String targetPlatform = platformOptionMixin.platform;
        String contestId = contestIdOptionMixin.contestId;

        System.out.println("Executing 'list-tasks' command...");
        System.out.println("Target Platform: " + targetPlatform);
        System.out.println("Contest ID: " + contestId);

        List<Task> tasks = platformService.getTasksForContest(targetPlatform, contestId);

        if (tasks == null || tasks.isEmpty()) {
            System.out.println("No tasks found for contest '" + contestId + "' on platform '" + targetPlatform + "'.");
        } else {
            System.out.println("Tasks for contest '" + contestId + "' on platform '" + targetPlatform + "':");
            for (Task task : tasks) {
                System.out.println("  ID: " + task.getId() + ", Name: " + task.getName());
            }
        }

        return 0;
    }
}