package po25.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import po25.*;
import po25.SatoriContest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ContestWindowController {
    @FXML private Label                contestLabel;
    @FXML private ListView<Task>       tasksList;
    @FXML private ListView<Submission> subsList;
    @FXML private Label                infoLabel;

    private Platform platform;
    private Contest  contest;

    void init(Platform p, Contest c) {
        platform = p;
        contest  = c;
        contestLabel.setText(c.getTitle());
        setupTaskViewer();
        setupSubsViewer();
        loadTasksAsync();
        loadSubsAsync();
    }

    private void setupTaskViewer() {
        tasksList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Task t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? null : t.getName());
            }
        });
        tasksList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Task t = tasksList.getSelectionModel().getSelectedItem();
                if (t != null) showContent(t);
            }
        });
    }

    private void setupSubsViewer() {
        subsList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Submission s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null
                        ? null
                        : s.getSubmissionId() + " : "  + s.getVerdict());
            }
        });
    }

    private void showContent(Task t) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(t.getName());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TextArea contentArea = new TextArea(t.getContent());
        contentArea.setWrapText(true);
        contentArea.setEditable(false);

        ScrollPane scroll = new ScrollPane(contentArea);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(300);

        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().setPrefSize(600, 350);
        dialog.showAndWait();
    }

    private void loadTasksAsync() {
        javafx.concurrent.Task<List<Task>> task = new javafx.concurrent.Task<>() {
            @Override protected List<Task> call() throws Exception {
                return contest.getTasks();
            }
        };
        task.setOnSucceeded(e ->
                tasksList.setItems(FXCollections.observableArrayList(task.getValue()))
        );
        task.setOnFailed(e -> infoLabel.setText("Failed to load tasks"));
        new Thread(task).start();
    }

    private void loadSubsAsync() {
        javafx.concurrent.Task<List<Submission>> task = new javafx.concurrent.Task<>() {
            @Override protected List<Submission> call() throws Exception {
                if (contest instanceof SatoriContest sc) {
                    sc.loadSubmissions();
                    return sc.getSubmissionHistory();
                }
                else if(contest instanceof CfContest cc){
                    return cc.getSubmissionHistory();
                }
                return List.of();
            }
        };
        task.setOnSucceeded(e ->
                subsList.setItems(FXCollections.observableArrayList(task.getValue()))
        );
        task.setOnFailed(e -> infoLabel.setText("Failed to load submissions"));
        new Thread(task).start();
    }

    @FXML private void handleSubmitAction() {
        Task t = tasksList.getSelectionModel().getSelectedItem();
        if (t == null) { infoLabel.setText("Pick a task first"); return; }

        ChoiceDialog<String> ld = new ChoiceDialog<>("Java", List.of("Java", "C++", "Python"));
        Optional<String> lang = ld.showAndWait();
        if (lang.isEmpty()) return;

        String ext = switch (lang.get()) {
            case "Java"   -> "java";
            case "C++"    -> "cpp";
            case "Python" -> "py";
            default       -> "txt";
        };

        TextInputDialog cd = new TextInputDialog();
        cd.setHeaderText("Enter code:");
        Optional<String> code = cd.showAndWait();
        if (code.isEmpty()) return;

        javafx.concurrent.Task<Submission> task = new javafx.concurrent.Task<>() {
            @Override protected Submission call() throws Exception {
                Path tmp = Files.createTempFile("subm_", "." + ext);
                Files.writeString(tmp, code.get());
                Submission sub = platform.submitSolution(t, tmp.toString(), lang.get());
                Files.deleteIfExists(tmp);
                return sub;
            }
        };
        task.setOnSucceeded(e -> {
            Submission sub = task.getValue();
            infoLabel.setText("Submitted: " + sub.getSubmissionId() + " → " + sub.getVerdict());
            loadSubsAsync();
        });
        task.setOnFailed(e -> infoLabel.setText("Submit failed"));
        new Thread(task).start();
    }
}
