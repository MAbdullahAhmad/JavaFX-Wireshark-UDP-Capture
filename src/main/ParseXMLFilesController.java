// Package
package main;

// Imports

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
        List<Map<String, Object>> results = parse_multilpe_xml_files(selectedFiles);

        // Print resutls
        for (Map<String, Object> single_result : results) {
            System.out.println(single_result);
        }

    }

    // Overloaded: default 'only_three' param is set to true
    public void handleFileSelection() { handleFileSelection(true); }


    /**
     * Parse given XML Files
     */
    private List<Map<String, Object>> parse_multilpe_xml_files(List<File> files) {
        List<Map<String, Object>> results = new ArrayList();

        // Parse and print XML files as key-value pairs
        for (File file : files) {
            results.add(parse_xml_file(file));
        }

        return results;

    }


    /**
     * single XML file parse
     */
    private Map<String, Object> parse_xml_file(File file) {
        try {
            // Create document builder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            // Normalize the XML structure
            document.getDocumentElement().normalize();

            // Start parsing from the document's root element
            return recursive_parse_helper(document.getDocumentElement());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Recursive helper to parse XML File data
     * parse XML nodes into a nested HashMap
     */
    private Map<String, Object> recursive_parse_helper(Node node) {
        Map<String, Object> result = new HashMap<>();
        
        // Process only element nodes
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            // Attributes data
            Map<String, Object> attributes_map = new HashMap<>();

            // if attributes, get
            if (node.hasAttributes()) {
                NamedNodeMap attributes = node.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node attr = attributes.item(i);
                    attributes_map.put(attr.getNodeName(), attr.getNodeValue());
                }
            }

            // If there are attributes, add them to the result under "#attributes"
            if (!attributes_map.isEmpty()) {
                result.put("#attributes", attributes_map);
            }

            // 
            // Process child nodes
            // 

            // Variables
            NodeList node_list = node.getChildNodes();
            Map<String, Object> children_map = new HashMap<>();
            boolean has_single_text_child = false;
            String text_content = null;

            // Iterate & Process Nodes
            for (int i = 0; i < node_list.getLength(); i++) {
                Node childNode = node_list.item(i);

                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Map<String, Object> child = recursive_parse_helper(childNode);
                    if (children_map.containsKey(childNode.getNodeName())) {
                        Object existingChild = children_map.get(childNode.getNodeName());
                        if (existingChild instanceof List) {
                            ((List<Object>) existingChild).add(child);
                        } else {
                            List<Object> newList = new ArrayList<>();
                            newList.add(existingChild);
                            newList.add(child);
                            children_map.put(childNode.getNodeName(), newList);
                        }
                    } else {
                        children_map.put(childNode.getNodeName(), child);
                    }
                }
                else if (childNode.getNodeType() == Node.TEXT_NODE) {
                    text_content = childNode.getTextContent().trim();
                    if (!text_content.isEmpty()) {
                        has_single_text_child = true;
                    }
                }
            }

            // If there are child elements, add them to the result map
            if (!children_map.isEmpty()) {
                result.putAll(children_map);
            }

            // If the node has only text content, return the text directly
            if (has_single_text_child && result.isEmpty()) {
                result.put(node.getNodeName(), text_content);
            }
            
            // Otherwise add as '#text'
            else if (!result.isEmpty() && text_content != null) {
                // Combine text content and child elements
                result.put("#text", text_content);
            }

        }

        // Returns result
        return result;
    }


}
