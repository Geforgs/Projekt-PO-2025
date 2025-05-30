package po25.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import po25.PlatformException;
import po25.Task;
import po25.commands.mixins.ContestIdOptionMixin;
import po25.commands.mixins.PlatformOptionMixin;
import po25.commands.mixins.TaskIdOptionMixin;
import po25.service.PlatformService;

import java.util.Optional;
import java.util.concurrent.Callable;

@Command(name = "view-task",
        aliases = {"vt"},
        description = "Views a specific task in contest and optionally saves it.",
        mixinStandardHelpOptions = true)
public class ViewTaskCommand implements Callable<Integer> {

    @CommandLine.Mixin
    private PlatformOptionMixin platformOptionMixin;

    @CommandLine.Mixin
    private ContestIdOptionMixin contestIdMixin;

    @CommandLine.Mixin
    private TaskIdOptionMixin taskIdOptionMixin;

    private PlatformService platformService;

    public ViewTaskCommand() {
        this.platformService = new PlatformService();
    }

    @Override
    public Integer call() {
        String platformName = platformOptionMixin.platform;
        String contestId = contestIdMixin.contestId;
        String taskId = taskIdOptionMixin.taskId;

        System.out.println("Fetching details for task '" + taskId + "' in contest '" + contestId + "' on platform '" + platformName + "'...");

        try {
            Optional<Task> taskOptional = platformService.getTaskInContest(platformName, contestId, taskId);

            if (taskOptional.isPresent()) {
                Task task = taskOptional.get();
                displayTaskDetails(task);
            } else {
                System.out.println("Task '" + taskId + "' in contest '" + contestId + "' not found on platform '" + platformName + "'.");
            }
            return 0;
        } catch (PlatformException e) {
            System.err.println("Error fetching task details from '" + platformName + "': " + e.getMessage());
            // e.printStackTrace();
            return 1;
        }
    }

    private void displayTaskDetails(Task task) {
        System.out.println("\n--- Task Details ---");
        System.out.println("Task ID:       " + task.getId());
        System.out.println("Task Name:     " + task.getName());
        System.out.println("--------------------");

        System.out.println("\nContent/Problem Statement:");
        try {
            System.out.println(task.getContent());
        } catch (Exception e) {
            System.out.println("[Error fetching task content: " + e.getMessage() + "]");
        }
        System.out.println("--------------------");

        task.getSampleInput().ifPresent(input -> {
            System.out.println("\nSample Input:");
            System.out.println(input);
            System.out.println("--------------------");
        });

        task.getSampleOutput().ifPresent(output -> {
            System.out.println("\nSample Output:");
            System.out.println(output);
            System.out.println("--------------------");
        });

        task.getTimeLimit().ifPresent(limit -> System.out.println("Time Limit:    " + limit));
        task.getMemoryLimit().ifPresent(limit -> System.out.println("Memory Limit:  " + limit));
        System.out.println("--------------------");
    }
}