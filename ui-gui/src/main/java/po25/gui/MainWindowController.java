package po25.gui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import po25.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MainWindowController {
    @FXML private ComboBox<String>  platformCombo;
    @FXML private Label             statusBarLabel;
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
            if (e.getClickCount() == 2 && contestsList.getSelectionModel().getSelectedItem() != null)
                openContestWindow(contestsList.getSelectionModel().getSelectedItem());
        });
    }


    @FXML private void handleLoginAction() {
        String sel = platformCombo.getValue();
        if (sel == null) { statusBarLabel.setText("Choose platform"); return; }
        TextInputDialog ud = new TextInputDialog(); ud.setHeaderText("Username:");
        Optional<String> u = ud.showAndWait(); if (u.isEmpty()) return;
        Dialog<String> pd = new Dialog<>();
        pd.setHeaderText("Password:");
        pd.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        PasswordField pf = new PasswordField();
        pf.setPromptText("Password");
        pd.getDialogPane().setContent(pf);
        pd.setResultConverter(btn -> btn == ButtonType.OK ? pf.getText() : null);
        Optional<String> p = pd.showAndWait();
        if (p.isEmpty() || p.get().isBlank()) {
            statusBarLabel.setText("Login cancelled");
            return;
        }

        platform = sel.equals("Codeforces") ? new CodeforcesPlatform() : new SatoriPlatform();
        try {
            platform.login(u.get(), p.get().toCharArray());
            statusBarLabel.setText("Logged in to " + sel);
            loadContestsAsync();
        } catch (PlatformException ex) {
            statusBarLabel.setText(ex.getMessage());
        } catch (LoginException ex) {
            statusBarLabel.setText(ex.getMessage());
        } catch (ConnectionException ex) {
            statusBarLabel.setText(ex.getMessage());
        } catch (RobotCheckException ex) {
            // TODO
        }
    }
    @FXML private void handleChromePathAction() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Chrome Path");
        dialog.setHeaderText("Enter path to Chrome executable:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(path -> {
            Browser.setPathToChrome(path);
            try {
                Browser.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    @FXML private void handleLogoutAction() {
        if (platform != null) { try { platform.logout(); } catch (Exception ignored) {} }
        platform = null;
        contestsList.getItems().clear();
        statusBarLabel.setText("Logged out");
    }

    @FXML private void handleRefreshAction() {
        if (platform == null) { statusBarLabel.setText("Login first"); return; }
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
            statusBarLabel.setText("Contests: " + cs.size());
        });
        task.setOnFailed(e -> statusBarLabel.setText("Failed: " + task.getException().getMessage()));
        new Thread(task).start();
    }

    private void openContestWindow(Contest contest) {
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("/po25/gui/ContestWindow.fxml"));
            Stage st = new Stage();
            st.setScene(new Scene(fx.load()));
            st.setTitle(contest.getTitle());
            ContestWindowController cwc = fx.getController();
            cwc.init(platform, contest);
            st.show();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }
}