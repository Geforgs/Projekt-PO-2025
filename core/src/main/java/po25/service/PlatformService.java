package po25.service;

import po25.*;

// import po25.SatoriPlatform;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PlatformService {

    private final Map<String, Platform> registeredPlatforms;

    public PlatformService() {
        this.registeredPlatforms = new HashMap<>();
        loadAndRegisterPlatforms();
    }

    private void loadAndRegisterPlatforms() {
        try {
            Platform codeforces = new CodeforcesPlatform();
            registeredPlatforms.put(codeforces.getPlatformName().toLowerCase(), codeforces);
        } catch (Exception e) {
            System.err.println("Error initializing Codeforces platform: " + e.getMessage());
        }

         try {
             Platform satori = new SatoriPlatform();
             registeredPlatforms.put(satori.getPlatformName().toLowerCase(), satori);
         } catch (Exception e) {
             System.err.println("Error initializing Satori platform: " + e.getMessage());
         }

    }

    /**
     * Retrieves a platform implementation by its name.
     *
     * @param platformName The name of the platform (case-insensitive).
     * @return The Platform implementation.
     * @throws PlatformException if the platform is not found or not registered.
     */
    private Platform getPlatform(String platformName) throws PlatformException {
        if (platformName == null || platformName.trim().isEmpty()) {
            throw new PlatformException("Platform name cannot be null or empty.");
        }
        Platform platform = registeredPlatforms.get(platformName.toLowerCase());
        if (platform == null) {
            throw new PlatformException("Platform '" + platformName + "' is not registered or supported. Available: " + registeredPlatforms.keySet());
        }
        return platform;
    }

    /**
     * Logs in to a specified platform.
     *
     * @param platformName The name of the platform.
     * @param username     The username.
     * @param password     The password.
     * @throws PlatformException if login fails or platform is not found.
     */
    public void login(String platformName, String username, String password) throws PlatformException {
        Platform platform = getPlatform(platformName);

        if (!platform.isSessionValid()) {
            platform.login(username, password);
            System.out.println("Successfully logged into " + platformName);
        } else {
            System.out.println("You were already logged into " + platformName);
        }
    }

    /**
     * Logs out from a specified platform.
     *
     * @param platformName The name of the platform.
     * @throws PlatformException if logout fails or platform is not found.
     */
    public void logout(String platformName) throws PlatformException {
        Platform platform = getPlatform(platformName);
        if (platform.isSessionValid()) {
            platform.logout();
            System.out.println("Successfully logged out from " + platformName);
        }

        System.out.println("You were not logged into " + platformName);
    }

    /**
     * Checks if the session is valid for a specified platform.
     *
     * @param platformName The name of the platform.
     * @return true if the session is valid, false otherwise.
     * @throws PlatformException if the platform is not found.
     */
    public boolean isSessionValid(String platformName) throws PlatformException {
        Platform platform = getPlatform(platformName);
        return platform.isSessionValid();
    }

    /**
     * Retrieves all contests for a given platform.
     *
     * @param platformName The name of the platform.
     * @return A list of contests.
     * @throws PlatformException if fetching contests fails or platform is not found.
     */
    public List<Contest> getContests(String platformName) throws PlatformException {
        Platform platform = getPlatform(platformName);
        // if (!platform.isSessionValid()) {
        //     throw new PlatformException("Not logged into " + platformName + ". Please login first.");
        // }
        return platform.getAllContests();
    }

    /**
     * Retrieves a specific contest by its ID from a given platform.
     *
     * @param platformName The name of the platform.
     * @param contestId    The ID of the contest.
     * @return An Optional containing the Contest if found, otherwise empty.
     * @throws PlatformException if fetching the contest fails or platform is not found.
     */
    public Optional<Contest> getContestById(String platformName, String contestId) throws PlatformException {
        Platform platform = getPlatform(platformName);
        return platform.getContestById(contestId);
    }

    /**
     * Retrieves all tasks for a specific contest on a given platform.
     * This is the method your ListTasksCommand will use.
     *
     * @param platformName The name of the platform.
     * @param contestId    The ID of the contest.
     * @return A list of tasks for the specified contest. Returns an empty list if contest or tasks are not found.
     * @throws PlatformException if there's an issue communicating with the platform or the platform/contest is invalid.
     */
    public List<Task> getTasksForContest(String platformName, String contestId) throws PlatformException {
        Platform platform = getPlatform(platformName);

        Optional<Contest> contestOptional = platform.getContestById(contestId);
        if (contestOptional.isPresent()) {
            Contest contest = contestOptional.get();
            return contest.getTasks();
        } else {
            System.err.println("Contest '" + contestId + "' not found on platform '" + platformName + "'.");
            return Collections.emptyList();
        }
    }

    /**
     * Retrieves a specific task by its ID from a specific contest on a given platform.
     *
     * @param platformName The name of the platform.
     * @param contestId    The ID of the contest.
     * @param taskId       The ID of the task.
     * @return An Optional containing the Task if found, otherwise empty.
     * @throws PlatformException if there's an issue.
     */
    public Optional<Task> getTaskInContest(String platformName, String contestId, String taskId) throws PlatformException {
        Optional<Contest> contestOptional = getContestById(platformName, contestId);
        if (contestOptional.isPresent()) {
            return contestOptional.get().getTaskById(taskId);
        }
        return Optional.empty();
    }
}