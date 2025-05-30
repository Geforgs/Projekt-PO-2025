package po25;

import java.time.LocalDateTime;
import java.util.Optional;

public class SatoriSubmission implements Submission {
    private final SatoriTask task;
    private final String id;
    private final LocalDateTime time;


    protected SatoriSubmission(SatoriTask task, String id, LocalDateTime time){
        this.task = task;
        this.id = id;
        this.time = time;
    }

    @Override
    public String getSubmissionId() {
        return this.id;
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
    public String getVerdict() {
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
