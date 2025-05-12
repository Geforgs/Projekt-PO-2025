package po25;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SubmissionResult {
    String getSubmissionId();

    String getTaskId();

    Optional<String> getContestId();

    String getVerdict(); // e.g., "Accepted", "Wrong Answer", "Time Limit Exceeded", "Compiling", "Running"

    Optional<String> getExecutionTime(); // e.g., "123 ms"

    Optional<String> getMemoryUsed();    // e.g., "16 MB"

    Optional<LocalDateTime> getSubmittedAt();

    Optional<String> getLanguage();

    Optional<String> getPoints();
}