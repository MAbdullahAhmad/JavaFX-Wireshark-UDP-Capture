import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ParseXMLFiles extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Select XML Files");

        // Button to open file chooser
        Button button = new Button("Select 3 XML Files");
        button.setOnAction(e -> handleFileSelection(primaryStage));

        VBox vbox = new VBox(button);
        vbox.setAlignment(Pos.CENTER);
        Scene scene = new Scene(vbox, 300, 150);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleFileSelection(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        // Select 3 XML files
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
        if (selectedFiles == null || selectedFiles.size() != 3) {
            System.out.println("Please select exactly 3 XML files.");
            return;
        }

        // Parse and print XML files
        for (File file : selectedFiles) {
            List<List<String>> parsedData = parseXML(file);
            System.out.println("Parsed data from file: " + file.getName());
            for (List<String> list : parsedData) {
                System.out.println(list);
            }
        }
    }

    // Method to parse XML file and return a list of lists (for simplicity)
    private List<List<String>> parseXML(File file) {
        List<List<String>> resultList = new ArrayList<>();
        try {
            // Create document builder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            // Normalize the XML structure
            document.getDocumentElement().normalize();

            // Example of parsing: assuming we're extracting all elements named "item"
            NodeList nodeList = document.getElementsByTagName("item");

            for (int i = 0; i < nodeList.getLength(); i++) {
                List<String> innerList = new ArrayList<>();
                innerList.add(nodeList.item(i).getTextContent()); // Add content of each <item>
                resultList.add(innerList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
