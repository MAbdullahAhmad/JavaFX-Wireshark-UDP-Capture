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

        // // Print to log
        // udpCapture.listen_and_capture_udp(
        //     "127.0.0.1", 5555, 
        //     message -> System.out.println("Message: " + message),
        //     error -> System.err.println("Error: " + error.getMessage()),
        //     "START",
        //     "STOP"
        // );

        // Save to DB
        udpCapture.listen_and_capture_udp(
            "127.0.0.1", 5555, 
            message -> udpCapture.save_message_to_db(message),  // Save to DB
            error -> System.err.println("Error: " + error.getMessage()),
            "START",
            "STOP"
        );

        // Other code can run while UDP listener works asynchronously
        System.out.println("Listener started.");
    }
}
