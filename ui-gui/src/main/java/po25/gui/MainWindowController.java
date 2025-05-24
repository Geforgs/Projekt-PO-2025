package po25.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
// Import other necessary JavaFX components if you want to reference them from the code

public class MainWindowController {

    // Example of injecting a component from FXML, if it has fx:id="statusBarLabel"
    @FXML
    private Label statusBarLabel;

    // Initialization method, called after the FXML is loaded
    @FXML
    public void initialize() {
        // Here you can initialize components, load initial data, etc.
        statusBarLabel.setText("Application started.");
        System.out.println("MainWindowController initialized.");
    }

    // Example event handler methods for MenuItems from the menu bar
    @FXML
    private void handleSettingsAction() {
        System.out.println("Action: Settings...");
        // Here you will open the settings dialog window
    }

    @FXML
    private void handleExitAction() {
        System.out.println("Action: Exit...");
        // Close the application
        // javafx.application.Platform.exit(); // or System.exit(0);
        // You can also get the Stage and close it:
        // Stage stage = (Stage) statusBarLabel.getScene().getWindow();
        // stage.close();
        javafx.application.Platform.exit();
    }

    @FXML
    private void handleLoginAction() {
        System.out.println("Action: Log in...");
        // Here you will open the login dialog window
    }

    @FXML
    private void handleLogoutAction() {
        System.out.println("Action: Log out...");
        // Logout logic
    }

    @FXML
    private void handleRefreshAction() {
        System.out.println("Action: Refresh data...");
        // Logic for refreshing data from PlatformService
        statusBarLabel.setText("Refreshing data...");
    }

    @FXML
    private void handleAboutAction() {
        System.out.println("Action: About...");
        // Here you will open the "About" dialog window
    }

    // You can add more methods here for interacting with PlatformService
    // and updating views (lists of contests, tasks, etc.)
}