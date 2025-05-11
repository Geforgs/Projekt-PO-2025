package po25.service;

import po25.Contest;
import po25.Platform;
import po25.PlatformException;
import po25.Task;

import po25.;
// import po25.SatoriPlatform;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader; // For a more dynamic way to load platforms

public class PlatformService {

    private final Map<String, Platform> registeredPlatforms;

    public PlatformService() {
        this.registeredPlatforms = new HashMap<>();
        // Initialize and register available platform implementations
        loadAndRegisterPlatforms();
    }

    private void loadAndRegisterPlatforms() {
        // Method 1: Manual Registration (Simpler for fewer platforms)
        // Ensure these platform classes are public and have a public no-arg constructor.
        // Also, the 'core' module's pom.xml must have dependencies on 'platform-codeforces', etc.
        try {
            Platform codeforces = new CodeforcesPlatform(); // Assumes CodeforcesPlatform implements po25.Platform
            registeredPlatforms.put(codeforces.getPlatformName().toLowerCase(), codeforces);
        } catch (Exception e) {
            // Handle issues with platform instantiation, e.g., log an error
            System.err.println("Error initializing Codeforces platform: " + e.getMessage());
        }

        // Example for Satori if/when implemented:
        // try {
        //     Platform satori = new SatoriPlatform(); // Assumes SatoriPlatform implements po25.Platform
        //     registeredPlatforms.put(satori.getPlatformName().toLowerCase(), satori);
        // } catch (Exception e) {
        //     System.err.println("Error initializing Satori platform: " + e.getMessage());
        // }

        // Method 2: Using ServiceLoader (More dynamic and extensible)
        // This is generally preferred for decoupling. Each platform module would provide
        // a service implementation file in META-INF/services/po25.Platform
        /*
        ServiceLoader<Platform> platformLoader = ServiceLoader.load(Platform.class);
        for (Platform platform : platformLoader) {
            registeredPlatforms.put(platform.getPlatformName().toLowerCase(), platform);
            System.out.println("Registered platform via ServiceLoader: " + platform.getPlatformName());
        }
        if (registeredPlatforms.isEmpty() && !"Method 1 was used above") {
             System.err.println("No platforms were loaded by ServiceLoader. Check META-INF/services configurations.");
        }
        */
        // For now, we'll stick to manual registration above for simplicity in this example.
        // If using ServiceLoader, remove the manual registrations or ensure they don't conflict.

        if (registeredPlatforms.isEmpty()) {
            System.err.println("Warning: No platforms were registered in PlatformService.");
        } else {
            System.out.println("PlatformService initialized. Registered platforms: " + registeredPlatforms.keySet());
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
        Platform platform = getPlatform(platformName); // Throws PlatformException if platform not found

        Optional<Contest> contestOptional = platform.getContestById(contestId);
        if (contestOptional.isPresent()) {
            Contest contest = contestOptional.get();
            return contest.getTasks(); // Assuming Contest.getTasks() returns List<Task>
        } else {
            // Contest not found, you could throw an exception or return empty list
            // For CLI, returning an empty list and letting the command report "not found" is often fine.
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

    // --- Placeholder methods for functionalities defined in your Platform interface ---
    // You'll need to define classes like Solution, Submission, SubmissionResult in platform-api
    // and then implement these methods fully.

    /*
    public String submitSolution(String platformName, Task task, String solutionCode, String languageId) throws PlatformException {
        Platform platform = getPlatform(platformName);
        // Ensure task is valid and from the correct platform if necessary
        // return platform.submitSolution(task, solutionCode, languageId);
        throw new UnsupportedOperationException("submitSolution not yet implemented in PlatformService.");
    }

    public SubmissionResult getSubmissionStatus(String platformName, String submissionId) throws PlatformException {
        Platform platform = getPlatform(platformName);
        // return platform.getSubmissionStatus(submissionId);
        throw new UnsupportedOperationException("getSubmissionStatus not yet implemented in PlatformService.");
    }

    public List<Submission> getSubmissionHistory(String platformName, Task task) throws PlatformException {
        Platform platform = getPlatform(platformName);
        // return platform.getSubmissionHistory(task);
        throw new UnsupportedOperationException("getSubmissionHistory not yet implemented in PlatformService.");
    }
    */
}