package main;

import javafx.stage.FileChooser;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Map;

import main.ParseXMLFiles;


public class ParseXMLFilesExample extends Application {

    private Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

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
        stage = primaryStage;
        selectFilesButton.setOnAction(e -> this.handleFileSelection());

        // Add the button to the layout
        vbox.getChildren().add(selectFilesButton);

        // Create and return a new scene with the VBox layout
        return new Scene(vbox, 300, 150);
    }


    // Callback
    public void handleFileSelection(boolean only_three) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        // Select 3 XML files (only_three=true)
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
        if(only_three){
            if (selectedFiles == null || selectedFiles.size() != 3) {
                System.out.println("CANNOT-PARSE: Please select exactly 3 XML files when 'only_three' argument is set to true.");
                return;
            }
        }

        // Select any number of files (only_three=false)
        else if (selectedFiles == null){
            System.out.println("CANNOT-PARSE: At least one xml file is required.");
            return;
        }

        // Use 'parse_multilpe_xml_files' function to parse files

        ParseXMLFiles parser = new ParseXMLFiles();
        List<Map<String, Object>> results = parser.parse_multilpe_xml_files(selectedFiles);

        // Print resutls
        for (Map<String, Object> single_result : results) {
            System.out.println(single_result);
        }

    }
    // Overloaded: default 'only_three' param is set to true
    public void handleFileSelection() { handleFileSelection(true); }


}
