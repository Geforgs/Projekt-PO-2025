package po25.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import po25.Contest;
import po25.PlatformException;
import po25.commands.mixins.PlatformOptionMixin;
import po25.service.PlatformService;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "list-contests",
        aliases = {"lc"},
        description = "Lists available contests on the specified platform.",
        mixinStandardHelpOptions = true)
public class ListContestsCommand implements Callable<Integer> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int DEFAULT_CONTEST_LIMIT = 10;

    @CommandLine.Mixin
    private PlatformOptionMixin platformOptionMixin;

    @CommandLine.Option(names = {"-n", "--limit"},
            description = "Number of recent contests to display (default: " + DEFAULT_CONTEST_LIMIT + ", all: -1).",
            defaultValue = "" + DEFAULT_CONTEST_LIMIT)
    private int limit;

    private PlatformService platformService;

    public ListContestsCommand() {
        this.platformService = new PlatformService();
    }

    @Override
    public Integer call() {
        String platformName = platformOptionMixin.platform;

        if (limit <= 0 && limit != -1) {
            System.err.println("Error: Limit must be a positive number.");
            return 1;
        }

        System.out.println("Fetching contests for platform '" + platformName + "'...");
        try {
            List<Contest> contests = platformService.getContests(platformName);

            if (contests.isEmpty()) {
                System.out.println("No contests found on platform '" + platformName + "'.");
            } else {
                if (limit == -1) limit = contests.size();
                displayContestList(contests, limit);
            }
            return 0;
        } catch (PlatformException e) {
            System.err.println("Error fetching contests from '" + platformName + "': " + e.getMessage());
            // e.printStackTrace();
            return 1;
        }
    }


    /**
     * Displays a list of contests in a formatted table.
     *
     * @param contests       The list of Contest objects to display.
     * @param displayedLimit The limit that was used to fetch these contests, for display context.
     */
    private void displayContestList(List<Contest> contests, int displayedLimit) {
        System.out.println("\n--- Recent Contests (displaying up to " + displayedLimit + ", found " + contests.size() + ") ---");
        System.out.printf("%-20s | %-40s | %-20s | %-20s%n",
                "Contest ID", "Title", "Start Time", "End Time");
        System.out.println(String.join("", Collections.nCopies(130, "-")));

        int actualLimit = Math.min(displayedLimit, contests.size());
        for (int i = 0; i < actualLimit; i++) {
            Contest contest = contests.get(i);

            String startTimeStr = contest.getStartTime().map(t -> t.format(DATE_TIME_FORMATTER)).orElse("N/A");
            String endTimeStr = contest.getEndTime().map(t -> t.format(DATE_TIME_FORMATTER)).orElse("N/A");

            /**
             * TODO:
             * Contest -> getDescritption() method something is wrong
             */

            System.out.printf("%-20s | %-40s | %-20s | %-20s%n",
                    contest.getId(),
                    contest.getTitle(),
                    startTimeStr,
                    endTimeStr);

        }
        System.out.println("----------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("To view details of a specific contest, use the 'view-contest <Contest ID>' command.");
    }
}