package po25;

import java.util.List;
import java.util.Optional;

public interface Task {
    /**
     * Returns the ID of the task.
     *
     * @return task ID.
     */
    String getId();

    /**
     * Returns the name of the task.
     *
     * @return task name.
     */
    String getName();

    /**
     * Returns the content of the task. May contain HTML or other platform-dependent format.
     *
     * @return task content.
     */
    String getContent() throws PlatformException, ConnectionException, LoginException;

    /**
     * Returns sample input data for the task, if available.
     *
     * @return Optional containing the sample input or an empty Optional.
     */
    Optional<String> getSampleInput();

    /**
     * Returns sample output data for the task, if available.
     *
     * @return Optional containing the sample output or an empty Optional.
     */
    Optional<String> getSampleOutput();

    /**
     * Returns the time limit for task execution, if specified.
     * The format may vary (e.g., "1s", "2000ms").
     *
     * @return Optional containing the time limit or an empty Optional.
     */
    Optional<String> getTimeLimit();

    /**
     * Returns the memory limit for the task, if specified.
     * The format may vary (e.g., "256MB").
     *
     * @return Optional containing the memory limit or an empty Optional.
     */
    Optional<String> getMemoryLimit();

    Submission submit(String path) throws PlatformException, ConnectionException, LoginException;

    List<Submission> getSubmissionHistory() throws PlatformException, ConnectionException, LoginException;

    String getUnparsedContent() throws PlatformException, ConnectionException, LoginException;

    String getCss() throws PlatformException, ConnectionException, LoginException;
}