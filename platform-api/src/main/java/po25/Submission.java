package po25;

import java.time.LocalDateTime;
import java.util.Optional;

public interface Submission {
    String getSubmissionId();

    String getTaskId();

    Optional<String> getContestId();

    String getVerdict();

    Optional<LocalDateTime> getSubmittedAt();

    Optional<String> getLanguage();
}