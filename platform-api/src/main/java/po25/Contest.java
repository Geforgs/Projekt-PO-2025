package po25;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

/**
 * Interface representing a single contest (competition).
 */
public interface Contest {

    /**
     * Returns the title of the contest.
     * @return title of the contest.
     */
    String getTitle();

    /**
     * Returns the list of tasks available within this contest.
     * @return list of Task objects.
     */
    List<Task> getTasks() throws PlatformException;

    /**
     * Returns the description of the contest, if available.
     * @return Optional containing the description or an empty Optional.
     */
    Optional<String> getDescription();

    /**
     * Searches for and returns a task based on its identifier.
     * @param taskId task identifier.
     * @return Optional containing the Task object if a task with the given ID exists, otherwise an empty Optional.
     */
    Optional<Task> getTaskById(String taskId) throws PlatformException;

    /**
     * Returns the start date and time of the contest, if specified.
     * @return Optional containing the start date and time or an empty Optional.
     */
    Optional<LocalDateTime> getStartTime();

    /**
     * Returns the end date and time of the contest, if specified.
     * @return Optional containing the end date and time or an empty Optional.
     */
    Optional<LocalDateTime> getEndTime();
}