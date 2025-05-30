package po25.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/po25/gui/MainWindow.fxml"));
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Projekt PO 2025");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
