package po25.gui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import po25.*;
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

    private Object platform;
    private Contest contest;

    private final ObservableList<Submission> subsObservable = FXCollections.observableArrayList();
    private final Map<String,String> verdictCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> { Thread t = new Thread(r); t.setDaemon(true); return t; });

    void init(Object p, Contest c) {
        platform = p;
        contest = c;
        contestLabel.setText(c.getTitle());
        setupTaskViewer();

        subsList.setItems(subsObservable);
        colTask.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTaskId()));
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
                for (Submission s : all) verdictCache.put(s.getSubmissionId(), "QUE");
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
                String v = sub.getVerdict();    // blocking, but off UI
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
