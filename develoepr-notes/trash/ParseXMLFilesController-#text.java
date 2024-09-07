package controllers;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class ParseXMLFilesController {

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

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

        // Parse and print XML files as key-value pairs
        for (File file : selectedFiles) {
            Map<String, Object> parsedData = parseXMLAsKeyValue(file);
            System.out.println("Parsed data from file: " + file.getName());
            System.out.println(parsedData);  // Print the Map in its nested form
        }
    }

    // Method to parse XML file and return a nested Map, handling attributes and nested elements
    private Map<String, Object> parseXMLAsKeyValue(File file) {
        try {
            // Create document builder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            // Normalize the XML structure
            document.getDocumentElement().normalize();

            // Start parsing from the document's root element
            return recursiveParseForKeyValue(document.getDocumentElement());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Recursive method to parse XML nodes into a nested Map/HashMap structure
    private Map<String, Object> recursiveParseForKeyValue(Node node) {
        Map<String, Object> resultMap = new HashMap<>();
        
        // Process only element nodes
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            // If the node has attributes, store them as part of the result map
            if (node.hasAttributes()) {
                NamedNodeMap attributes = node.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node attr = attributes.item(i);
                    resultMap.put("@" + attr.getNodeName(), attr.getNodeValue());
                }
            }

            // Process child nodes
            NodeList nodeList = node.getChildNodes();
            Map<String, Object> childrenMap = new HashMap<>();
            boolean hasSingleTextChild = false;
            String textContent = null;

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node childNode = nodeList.item(i);

                // If the child is an element node, process it recursively
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Map<String, Object> child = recursiveParseForKeyValue(childNode);
                    if (childrenMap.containsKey(childNode.getNodeName())) {
                        // If multiple nodes of the same name exist, convert to a list
                        Object existingChild = childrenMap.get(childNode.getNodeName());
                        if (existingChild instanceof List) {
                            ((List<Object>) existingChild).add(child);
                        } else {
                            List<Object> newList = new ArrayList<>();
                            newList.add(existingChild);
                            newList.add(child);
                            childrenMap.put(childNode.getNodeName(), newList);
                        }
                    } else {
                        childrenMap.put(childNode.getNodeName(), child);
                    }
                }
                // If it's a text node, check for content
                else if (childNode.getNodeType() == Node.TEXT_NODE) {
                    textContent = childNode.getTextContent().trim();
                    if (!textContent.isEmpty()) {
                        hasSingleTextChild = true;
                    }
                }
            }

            // If there are child elements, add them to the result map
            if (!childrenMap.isEmpty()) {
                resultMap.putAll(childrenMap);
            }

            // If the node has only text content, return the text directly
            if (hasSingleTextChild && resultMap.isEmpty()) {
                resultMap.put(node.getNodeName(), textContent);
            } else if (!resultMap.isEmpty() && textContent != null) {
                // Combine text content and child elements
                resultMap.put("#text", textContent);
            }
        }
        return resultMap;
    }

}
