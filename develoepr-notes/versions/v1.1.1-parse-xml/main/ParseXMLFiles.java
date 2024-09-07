package main;

import main.ParseXMLFilesController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ParseXMLFiles extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Call the create_scene method to build the UI programmatically
        Scene scene = create_scene(primaryStage);

        primaryStage.setTitle("Select XML Files");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // This function creates the scene programmatically, adding UI elements like buttons
    public Scene create_scene(Stage primaryStage) {
        // Create a VBox layout to arrange elements vertically
        VBox vbox = new VBox(20); // 20 is the spacing between elements
        vbox.setAlignment(javafx.geometry.Pos.CENTER);

        // Create a button for selecting 3 XML files
        Button selectFilesButton = new Button("Select 3 XML Files");

        // Set action for the button
        ParseXMLFilesController controller = new ParseXMLFilesController();
        controller.setStage(primaryStage);
        selectFilesButton.setOnAction(e -> controller.handleFileSelection());

        // Add the button to the layout
        vbox.getChildren().add(selectFilesButton);

        // Create and return a new scene with the VBox layout
        return new Scene(vbox, 300, 150);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
