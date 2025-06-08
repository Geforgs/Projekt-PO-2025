package po25.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import po25.Browser;

import java.net.URL;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/po25/gui/MainWindow.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        URL css = getClass().getResource("/po25/gui/modern.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);
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
