package po25.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import po25.Browser;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/po25/gui/MainWindow.fxml"));
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Dccp");
        stage.show();
    }
    @Override
    public void stop() throws Exception {
        try {
            Browser.stop();
        } catch (Exception ignored) {}
        super.stop();
    }
    public static void main(String[] args) {
        launch();
    }
}
