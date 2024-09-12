package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.CaptureUDPController;

public class CaptureUDP extends Application {

    // Label to display status
    private Label status_label;

    @Override
    public void start(Stage stage) {
        // Initialize the controller
        CaptureUDPController controller = new CaptureUDPController(this);

        // Create the scene and set up the UI
        Scene scene = createScene(stage, controller);

        stage.setTitle("UDP Packet Capture");
        stage.setScene(scene);
        stage.show();

        // Simulate the steps you mentioned, calling the database functions
        controller.startCapture();
        controller.stopCapture();
        controller.saveCapturedData("Sample packet data for testing...");
    }

    // Method to create the scene with a label to show status messages
    public Scene createScene(Stage stage, CaptureUDPController controller) {
        VBox vbox = new VBox(20);  // VBox layout with 20px spacing
        vbox.setAlignment(javafx.geometry.Pos.CENTER);

        // Label to display the status of the capture process
        status_label = new Label("Awaiting status updates...");

        // Add the status label to the VBox layout
        vbox.getChildren().add(status_label);

        return new Scene(vbox, 400, 200);
    }

    // Method to update the status text in the UI
    public void updateStatus(String message) {
        status_label.setText(message);  // Update the status_label text
    }

    public static void main(String[] args) {
        launch(args);
    }
}
