package po25;

import java.time.LocalDateTime;
import java.util.Optional;

public interface Submission {
    String getSubmissionId();

    String getTaskId();

    Optional<String> getContestId();

    String getBriefVerdict(); // Could be same as SubmissionResult.getVerdict()

    Optional<LocalDateTime> getSubmittedAt();

    Optional<String> getLanguage();
}