package po25;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SubmissionResult {
    String getSubmissionId();

    String getTaskId();

    Optional<String> getContestId();

    // TODO:
    // getVerdict() String -> enum
    // getMemory and getExeTime -> special data type

    String getVerdict();

    Optional<String> getExecutionTime(); // "123 ms"

    Optional<String> getMemoryUsed();    // "16 MB"

    Optional<LocalDateTime> getSubmittedAt();

    Optional<String> getLanguage();

    Optional<String> getPoints();
}