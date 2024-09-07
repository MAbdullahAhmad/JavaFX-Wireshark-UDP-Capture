package controllers;

import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ParseXMLFilesController {

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void handleFileSelection() {
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

    // Method to parse XML file and return a list of lists, handling nested structures
    private List<List<String>> parseXML(File file) {
        List<List<String>> resultList = new ArrayList<>();
        try {
            // Create document builder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            // Normalize the XML structure
            document.getDocumentElement().normalize();

            // Start recursive parsing from the document's root element
            List<String> result = new ArrayList<>();
            recursiveParse(document.getDocumentElement(), result);
            resultList.add(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }

    // Recursive method to parse XML nodes and their children
    private void recursiveParse(Node node, List<String> result) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            result.add("<" + node.getNodeName() + ">");
            
            // Check if the node has any attributes
            if (node.hasAttributes()) {
                NamedNodeMap attributes = node.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node attr = attributes.item(i);
                    result.add("Attribute: " + attr.getNodeName() + " = " + attr.getNodeValue());
                }
            }

            // Check if the node has text content
            String content = node.getTextContent().trim();
            if (!content.isEmpty() && node.getChildNodes().getLength() == 1) {
                result.add("Content: " + content);
            }

            // Process child nodes recursively
            NodeList nodeList = node.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                recursiveParse(nodeList.item(i), result);
            }

            result.add("</" + node.getNodeName() + ">");
        }
    }

}
