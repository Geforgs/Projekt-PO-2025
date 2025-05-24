package po25.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            URL fxmlLocation = getClass().getResource("/po25/gui/MainWindow.fxml");
            if (fxmlLocation == null) {
                System.err.println("Cannot find FXML file: MainWindow.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Scene scene = new Scene(root, 1024, 768);

            primaryStage.setTitle("Desktop Client for Competitive Programming Platforms");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Error loading MainWindow.fxml:");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while starting the GUI application:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}