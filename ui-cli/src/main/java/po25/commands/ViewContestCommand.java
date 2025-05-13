package po25.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import po25.Contest;
import po25.PlatformException;
import po25.Task;
import po25.commands.mixins.ContestIdOptionMixin;
import po25.commands.mixins.PlatformOptionMixin;
import po25.service.PlatformService;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

@Command(name = "view-contest",
        aliases = {"vc"},
        description = "Views details of a specific contest, including its tasks, from a platform.",
        mixinStandardHelpOptions = true)
public class ViewContestCommand implements Callable<Integer> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @CommandLine.Mixin
    private PlatformOptionMixin platformOptionMixin;

    @CommandLine.Mixin
    private ContestIdOptionMixin contestIdOptionMixin;

    private PlatformService platformService;

    public ViewContestCommand() {
        this.platformService = new PlatformService();
    }

    @Override
    public Integer call() {
        String platformName = platformOptionMixin.platform;
        String contestIdToView = contestIdOptionMixin.contestId;

        System.out.println("Fetching details for contest '" + contestIdToView + "' on platform '" + platformName + "'...");
        try {
            Optional<Contest> contestOpt = platformService.getContestById(platformName, contestIdToView);

            if (contestOpt.isPresent()) {
                displayContestDetails(contestOpt.get());
            } else {
                System.out.println("Contest with ID '" + contestIdToView + "' not found on platform '" + platformName + "'.");
            }
            return 0;
        } catch (PlatformException e) {
            System.err.println("Error fetching contest details from '" + platformName + "': " + e.getMessage());
            // e.printStackTrace();
            return 1;
        }
    }

    private void displayContestDetails(Contest contest) throws PlatformException {
        System.out.println("\n--- Contest Details ---");
        System.out.println("ID:            " + contest.getId());
        System.out.println("Title:         " + contest.getTitle());
        contest.getDescription().ifPresent(desc -> {
            System.out.println("Description:   " + desc);
        });
        contest.getStartTime().ifPresent(time -> System.out.println("Start Time:    " + time.format(DATE_TIME_FORMATTER)));
        contest.getEndTime().ifPresent(time -> System.out.println("End Time:      " + time.format(DATE_TIME_FORMATTER)));
        System.out.println("-----------------------");

        List<Task> tasks = contest.getTasks();
        if (tasks == null || tasks.isEmpty()) {
            System.out.println("No tasks found for this contest or tasks are not yet loaded.");
        } else {
            System.out.println("\n--- Tasks in this Contest (" + tasks.size() + ") ---");
            System.out.printf("%-15s | %s%n", "Task ID", "Task Name");
            System.out.println(String.join("", Collections.nCopies(50, "-")));
            for (Task task : tasks) {
                System.out.printf("%-15s | %s%n", task.getId(), task.getName());
            }
            System.out.println("------------------------------------");
        }
    }
}