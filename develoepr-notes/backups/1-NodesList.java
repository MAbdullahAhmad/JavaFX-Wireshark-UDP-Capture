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

import java.util.HashMap;
import java.util.Map;

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
            // Prepare a map to hold this node's children or content
            Map<String, Object> childMap = new HashMap<>();
            
            // If the node has attributes, store them as part of the child map
            if (node.hasAttributes()) {
                NamedNodeMap attributes = node.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node attr = attributes.item(i);
                    childMap.put("@" + attr.getNodeName(), attr.getNodeValue());
                }
            }

            // Process child nodes
            NodeList nodeList = node.getChildNodes();
            Map<String, Object> childrenMap = new HashMap<>();
            List<Object> childrenList = new ArrayList<>();
            boolean hasSingleTextChild = false;

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
                    String textContent = childNode.getTextContent().trim();
                    if (!textContent.isEmpty()) {
                        hasSingleTextChild = true;
                        childMap.put(node.getNodeName(), textContent);
                    }
                }
            }

            // If there are child elements, add them to the current node
            if (!childrenMap.isEmpty()) {
                childMap.putAll(childrenMap);
            }

            // If the node has only text content, don't nest it further
            if (hasSingleTextChild && childMap.size() == 1) {
                return childMap; // Return text directly if no attributes or child elements
            }

            resultMap.put(node.getNodeName(), childMap);
        }
        return resultMap;
    }

}
