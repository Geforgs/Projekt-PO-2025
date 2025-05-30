package po25;

import java.time.LocalDateTime;
import java.util.Optional;

public class CfSubmission implements Submission {
    private final CfTask task;
    private final String submissionId;
    private final String url;
    private final LocalDateTime time;

    protected CfSubmission(CfTask task, String id, String url, LocalDateTime time) {
        this.task = task;
        this.submissionId = id;
        this.url = url;
        this.time = time;
    }

    @Override
    public String getSubmissionId() {
        return this.submissionId;
    }

    @Override
    public String getTaskId() {
        return this.task.getId();
    }

    @Override
    public Optional<String> getContestId() {
        return Optional.of(this.task.contest.getId());
    }

    @Override
    public String getBriefVerdict() {
        return "";
    }

    @Override
    public Optional<LocalDateTime> getSubmittedAt() {
        return Optional.of(this.time);
    }

    @Override
    public Optional<String> getLanguage() {
        return Optional.empty();
    }
}
