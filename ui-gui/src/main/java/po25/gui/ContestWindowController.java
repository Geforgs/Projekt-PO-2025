package po25.gui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import po25.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

public class ContestWindowController {
    @FXML private Label contestLabel;
    @FXML private ListView<po25.Task> tasksList;

    @FXML private TableView<Submission> subsList;
    @FXML private TableColumn<Submission,String> colTask;
    @FXML private TableColumn<Submission,String> colId;
    @FXML private TableColumn<Submission,String> colVerdict;

    @FXML private Label infoLabel;
    @FXML private Button submitButton;

    private po25.Platform platform;
    private Contest contest;

    private final ObservableList<Submission> subsObservable = FXCollections.observableArrayList();
    private final Map<String,String> verdictCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> { Thread t = new Thread(r); t.setDaemon(true); return t; });

    void init(po25.Platform p, Contest c) {
        platform = p;
        contest = c;
        contestLabel.setText(c.getTitle());
        setupTaskViewer();

        subsList.setItems(subsObservable);
        colTask.setCellValueFactory(cell -> {
            String taskId = cell.getValue().getTaskId();
            String taskName;
            try {
                taskName = contest.getTaskById(taskId)
                        .map(po25.Task::getName)
                        .orElse(taskId);
            } catch (PlatformException e) {
                taskName = taskId;
            } catch (LoginException e) {
                throw new RuntimeException(e);
            } catch (ConnectionException e) {
                throw new RuntimeException(e);
            }
            return new SimpleStringProperty(taskName);
        });
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSubmissionId()));
        colVerdict.setCellValueFactory(cell -> {
            String id = cell.getValue().getSubmissionId();
            return new SimpleStringProperty(verdictCache.getOrDefault(id, "..."));
        });

        loadTasksAsync();
        loadSubsAsync();
    }

    private void setupTaskViewer() {
        tasksList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(po25.Task t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? null : t.getName());
            }
        });
        tasksList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                po25.Task t = tasksList.getSelectionModel().getSelectedItem();
                if (t != null) showContent(t);
            }
        });
    }

    private void showContent(po25.Task t) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(t.getName());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        DialogPane dp = dialog.getDialogPane();
        URL cssUrl = getClass().getResource("/po25/gui/modern.css");
        if (cssUrl != null) dp.getStylesheets().add(cssUrl.toExternalForm());
        dp.getStyleClass().add("root-pane");

        TextArea contentArea;
        try {
            contentArea = new TextArea(t.getContent());
        } catch (Exception ex) {
            contentArea = new TextArea("Unable to load statement:\n" + ex.getMessage());
        }
        contentArea.setEditable(false);
        contentArea.setWrapText(true);
        contentArea.setPrefSize(600, 420);
        contentArea.getStyleClass().add("text-area");

        dp.setContent(contentArea);
        dialog.setResizable(true);
        dialog.showAndWait();
    }








    private void loadTasksAsync() {
        Task<List<po25.Task>> task = new Task<>() {
            @Override protected List<po25.Task> call() throws Exception {
                return contest.getTasks();
            }
        };
        task.setOnSucceeded(e ->
                tasksList.setItems(FXCollections.observableArrayList(task.getValue()))
        );
        task.setOnFailed(e ->
                Platform.runLater(() -> infoLabel.setText("Failed to load tasks"))
        );
        new Thread(task).start();
    }

    private void loadSubsAsync() {
        Task<List<Submission>> task = new Task<>() {
            @Override protected List<Submission> call() throws Exception {
                if (contest instanceof SatoriContest sc) {
                    sc.loadSubmissions();
                    return sc.getSubmissionHistory();
                } else if (contest instanceof CfContest cc) {
                    return cc.getSubmissionHistory();
                }
                return List.of();
            }
        };
        task.setOnSucceeded(e -> {
            List<Submission> all = task.getValue();
            Platform.runLater(() -> {
                subsObservable.setAll(all);
                for (Submission s : all) verdictCache.put(s.getSubmissionId(), "...");
                subsList.refresh();
            });
            all.forEach(this::pollUntilDone);
        });
        task.setOnFailed(e ->
                Platform.runLater(() -> infoLabel.setText("Failed to load submissions"))
        );
        new Thread(task).start();
    }
    @FXML
    private void handleChromePathAction() {

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Chrome Path");
        dialog.setHeaderText("Enter path to Chrome executable:");

        DialogPane dp = dialog.getDialogPane();
        URL css = getClass().getResource("/po25/gui/modern.css");
        if (css != null) dp.getStylesheets().add(css.toExternalForm());
        dp.getStyleClass().add("root-pane");

        ButtonType ok = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dp.getButtonTypes().addAll(ok, ButtonType.CANCEL);

        TextField pathField = new TextField();
        pathField.getStyleClass().add("input-purple");

        dp.setContent(pathField);

        dialog.setResultConverter(btn ->
                btn == ok ? pathField.getText().trim() : null);

        dialog.showAndWait().ifPresent(path -> {
            if (!path.isBlank()) {
                Browser.setPathToChrome(path);
                try { Browser.start(); }
                catch (IOException e) { throw new RuntimeException(e); }
            }
        });
    }







    @FXML
    private void handleSubmitAction() {
        po25.Task t = tasksList.getSelectionModel().getSelectedItem();
        if (t == null) {
            infoLabel.setText("Pick a task first");
            return;
        }
        Dialog<Pair<String,String>> dialog = new Dialog<>();
        dialog.setTitle("Submit Solution");
        dialog.setHeaderText("Choose language and paste code:");

        DialogPane dp = dialog.getDialogPane();
        URL cssUrl = getClass().getResource("/po25/gui/modern.css");
        if (cssUrl != null) dp.getStylesheets().add(cssUrl.toExternalForm());
        dp.getStyleClass().add("root-pane");

        ButtonType ok = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dp.getButtonTypes().addAll(ok, ButtonType.CANCEL);
        Button bOk     = (Button) dp.lookupButton(ok);
        Button bCancel = (Button) dp.lookupButton(ButtonType.CANCEL);
        bOk.getStyleClass().add("button-raised");
        bCancel.getStyleClass().add("button-raised");

        ComboBox<String> langBox = new ComboBox<>(FXCollections.observableArrayList("Java","C++","Python"));
        langBox.setValue("Java");
        langBox.getStyleClass().add("list-view");

        Label langLabel = new Label("Language:");
        langLabel.getStyleClass().add("header-label");
        Label codeLabel = new Label("Code:");
        codeLabel.getStyleClass().add("header-label");

        TextArea codeArea = new TextArea();
        codeArea.setWrapText(true);
        codeArea.setPrefSize(600, 400);
        codeArea.getStyleClass().add("list-view");

        VBox content = new VBox(8, langLabel, langBox, codeLabel, codeArea);
        content.setStyle("-fx-padding: 16;");
        dp.setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == ok) return new Pair<>(langBox.getValue(), codeArea.getText());
            return null;
        });

        Optional<Pair<String,String>> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().getValue().isBlank()) return;
        String lang = result.get().getKey();
        String code = result.get().getValue();
        String ext = switch(lang) {
            case "Java"   -> "java";
            case "C++"    -> "cpp";
            case "Python" -> "py";
            default       -> "txt";
        };

        submitButton.setDisable(true);
        Task<Submission> task = new Task<>() {
            @Override protected Submission call() throws Exception {
                Path tmp = Files.createTempFile("subm_", "." + ext);
                Files.writeString(tmp, code);
                Submission sub;
                if (platform instanceof SatoriPlatform sp) sub = sp.submitSolution(t, tmp.toString(), lang);
                else if (platform instanceof CodeforcesPlatform cf) sub = cf.submitSolution(t, tmp.toString(), lang);
                else throw new PlatformException("Unsupported platform");
                Files.deleteIfExists(tmp);
                return sub;
            }
        };
        task.setOnSucceeded(e -> {
            Submission sub = task.getValue();
            infoLabel.setText("Submitted: " + sub.getSubmissionId());
            submitButton.setDisable(false);
            verdictCache.put(sub.getSubmissionId(), "QUE");
            subsObservable.add(0, sub);
            subsList.scrollTo(0);
            pollUntilDone(sub);
        });
        task.setOnFailed(e -> {
            infoLabel.setText("Submit failed");
            submitButton.setDisable(false);
        });
        new Thread(task).start();
    }



    private void pollUntilDone(Submission sub) {
        final ScheduledFuture<?>[] future = new ScheduledFuture<?>[1];
        future[0] = scheduler.scheduleAtFixedRate(() -> {
            try {
                String v = sub.getVerdict();
                verdictCache.put(sub.getSubmissionId(), v);
                Platform.runLater(() -> {
                    subsList.refresh();
                    infoLabel.setText("Submission "+sub.getSubmissionId()+" → "+v);
                });
                if (!"QUE".equals(v)) future[0].cancel(false);
            } catch (Exception ex) {
                future[0].cancel(false);
            }
        }, 0, 3, TimeUnit.SECONDS);
    }
}
