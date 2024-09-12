package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CaptureUDP2Example extends Application {

    private TextArea statusArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Call the createScene method to build the UI programmatically
        Scene scene = createScene(primaryStage);

        primaryStage.setTitle("UDP Packet Capture");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start listening for UDP packets
        startListening();
    }

    // This function creates the scene programmatically
    public Scene createScene(Stage primaryStage) {
        VBox vbox = new VBox(20);  // VBox layout with 20px spacing
        vbox.setAlignment(javafx.geometry.Pos.CENTER);

        // Create a TextArea to display status
        statusArea = new TextArea();
        statusArea.setEditable(false);  // Disable editing by the user

        // Add the TextArea to the VBox layout
        vbox.getChildren().add(statusArea);

        // Create and return a new scene with the VBox layout
        return new Scene(vbox, 400, 200);
    }

    // Method to update the status text in the UI
    public void updateStatus(String message) {
        statusArea.appendText(message + "\n");  // Append message to the TextArea
    }

    // Method to start listening for UDP packets
    public void startListening() {
        CaptureUDP2 udpCapture = new CaptureUDP2();

        // Update status before starting
        updateStatus("Starting Listen & Capture");
        System.out.println("Starting Listen & Capture");

        udpCapture.listen_and_capture_udp(
            30,                            // Timeout in seconds
            "127.0.0.1",                   // Host
            5555,                          // Port
            true,                          // auto_stop_after_timeout
            true,                          // verbose
            "START",                       // Start signal
            "STOP",                        // Stop signal
            true,                          // include_signals_in_message
            message -> {                   // onDataReceived callback
                System.out.println("Message: " + message);
            },
            () -> {                        // onStop callback
                System.out.println("Stopped capturing.");
            },
            error -> {                     // onError callback
                System.err.println("Error: " + error.getMessage());
            }
        );
    }
}
