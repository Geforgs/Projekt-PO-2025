package po25.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import po25.*;
import javafx.geometry.Insets;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class MainWindowController {
    @FXML private ComboBox<String>  platformCombo;
    @FXML private ListView<Contest> contestsList;

    private Platform platform;

    @FXML private void initialize() {
        platformCombo.getItems().addAll("Codeforces", "Satori");
        contestsList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Contest c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getTitle());
            }
        });
        contestsList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && contestsList.getSelectionModel().getSelectedItem() != null) {
                openContestWindow(contestsList.getSelectionModel().getSelectedItem());
            }
        });
    }

    @FXML
    private void handleLoginAction() {
        String sel = platformCombo.getValue();
        if (sel == null) return;

        Dialog<javafx.util.Pair<String,String>> dialog = new Dialog<>();
        dialog.setTitle("Login");
        dialog.setHeaderText("Enter login and password");

        DialogPane dp = dialog.getDialogPane();
        URL css = getClass().getResource("/po25/gui/modern.css");
        if (css != null) dp.getStylesheets().add(css.toExternalForm());
        dp.getStyleClass().add("root-pane");

        ButtonType ok = new ButtonType("Log In", ButtonBar.ButtonData.OK_DONE);
        dp.getButtonTypes().addAll(ok, ButtonType.CANCEL);

        TextField userField = new TextField();
        userField.setPromptText("Username");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        //grid.add(new Label("Username:"), 0, 0);
        grid.add(userField,              1, 0);
        //grid.add(new Label("Password:"), 0, 1);
        grid.add(passField,              1, 1);
        dp.setContent(grid);
        userField.getStyleClass().add("button-raised");
        passField.getStyleClass().add("button-raised");
        Node loginButton = dp.lookupButton(ok);
        loginButton.disableProperty().bind(
                userField.textProperty().isEmpty()
                        .or(passField.textProperty().isEmpty())
        );

        dialog.setResultConverter(btn -> {
            if (btn == ok) {
                return new javafx.util.Pair<>(userField.getText(), passField.getText());
            }
            return null;
        });

        Optional<javafx.util.Pair<String,String>> result = dialog.showAndWait();
        if (result.isPresent()) {
            var creds = result.get();
            String username = creds.getKey();
            char[] password = creds.getValue().toCharArray();
            platform = sel.equals("Codeforces")
                    ? new CodeforcesPlatform()
                    : new SatoriPlatform();
            try {
                platform.login(username, password);
                loadContestsAsync();
            } catch (PlatformException ignored) {}
            catch (ConnectionException e){
            }catch (LoginException e){
            }catch (RobotCheckException e){
            }
        }
    }

    @FXML
    private void handleChromePathAction() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Chrome Path");
        dialog.setHeaderText("Enter path to Chrome executable:");

        DialogPane dp = dialog.getDialogPane();
        URL css = getClass().getResource("/po25/gui/modern.css");
        if (css != null) dp.getStylesheets().add(css.toExternalForm());
        dp.getStyleClass().add("root-pane");
        dialog.getEditor().getStyleClass().add("input-purple");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(path -> {
            Browser.setPathToChrome(path);
            try { Browser.start(); }
            catch (IOException e) { throw new RuntimeException(e); }
        });
    }





    @FXML private void handleLogoutAction() {
        if (platform != null) {
            try { platform.logout(); } catch (Exception ignored) {}
        }
        platform = null;
        contestsList.getItems().clear();
    }

    @FXML private void handleRefreshAction() {
        if (platform == null) {
            return;
        }
        loadContestsAsync();
    }

    private void loadContestsAsync() {
        javafx.concurrent.Task<List<Contest>> task = new javafx.concurrent.Task<>() {
            @Override protected List<Contest> call() throws Exception {
                return platform.getAllContests();
            }
        };
        task.setOnSucceeded(e -> {
            List<Contest> cs = task.getValue();
            contestsList.setItems(FXCollections.observableArrayList(cs));
        });

        new Thread(task).start();
    }

    private void openContestWindow(Contest contest) {
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("/po25/gui/ContestWindow.fxml"));
            Parent root = fx.load();

            Scene scene = new Scene(root);
            URL css = getClass().getResource("/po25/gui/modern.css");
            scene.getStylesheets().add(css.toExternalForm());

            Stage st = new Stage();
            st.setScene(scene);
            st.setTitle(contest.getTitle());

            ContestWindowController cwc = fx.getController();
            cwc.init(platform, contest);

            st.show();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK)
                    .showAndWait();
        }
    }
}
