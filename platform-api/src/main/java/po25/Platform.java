package po25;

import java.util.List;
import java.util.Optional;

/**
 * Interface representing a competition platform (e.g., Codeforces, Satori).
 * Responsible for interaction with the given platform.
 */
public interface Platform {

    /**
     * Returns the name of the platform.
     * @return platform name.
     */
    String getPlatformName();

    /**
     * Logs the user into the platform.
     * The implementation should securely handle credentials.
     * May throw exceptions in case of login failure (e.g., PlatformLoginException).
     * @param username username.
     * @param password user's password.
     */
    void login(String username, String password) throws PlatformException; // Rozważ dedykowany wyjątek

    /**
     * Checks if the current user session is still active/valid.
     * @return true if the session is valid, false otherwise.
     */
    boolean isSessionValid();

    /**
     * Logs the user out of the platform.
     */
    void logout();

    /**
     * Retrieves a list of all available (or e.g., watched) contests on the platform.
     * May throw exceptions in case of network or API problems (e.g., PlatformRequestException).
     * @return list of Contest objects.
     */
    List<Contest> getAllContests() throws PlatformException;

    /**
     * Retrieves a specific contest based on its identifier.
     * @param contestId contest identifier.
     * @return Optional containing the Contest object if a contest with the given ID exists, otherwise an empty Optional.
     */
    Optional<Contest> getContestById(String contestId) throws PlatformException;

    /**
     * Submits a solution to a task on the platform.
     * (Requires defining Solution and Submission classes or similar)
     * @param task the task to which the solution is submitted.
     * @param solutionCode source code of the solution.
     * @param languageId identifier of the programming language.
     * @return identifier of the submitted solution.
     */
    // String submitSolution(Task task, String solutionCode, String languageId) throws PlatformException;

    /**
     * Retrieves the status/result of a specific submission.
     * (Requires defining a SubmissionResult class or similar)
     * @param submissionId submission identifier.
     * @return object representing the submission result.
     */
    // SubmissionResult getSubmissionStatus(String submissionId) throws PlatformException;

    /**
     * Retrieves the submission history for a given task or user.
     * @return list of submissions.
     */
    // List<Submission> getSubmissionHistory(Task task) throws PlatformException;
}

