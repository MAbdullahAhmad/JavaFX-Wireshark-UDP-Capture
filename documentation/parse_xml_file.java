import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;    


/**
 * Parse given XML Files
 */
public List<Map<String, Object>> parse_multilpe_xml_files(List<File> files) {
    List<Map<String, Object>> results = new ArrayList();

    // Parse and print XML files as key-value pairs
    for (File file : files) {
        results.add(parse_xml_file(file));
    }

    return results;

}


/**
 * Parses an XML file and returns the data in the expected structure:
 * Each element has 'name', 'data', 'attributes', and 'children'.
 */
public Map<String, Object> parse_xml_file(File file) {
    Map<String, Object> result = new HashMap<>();
    try {
        // Create document builder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);

        // Normalize the XML structure
        document.getDocumentElement().normalize();

        // Stack to keep track of elements to process
        Stack<Node> stack = new Stack<>();
        Stack<Map<String, Object>> resultStack = new Stack<>();

        // Initialize with the root element
        Node root = document.getDocumentElement();
        stack.push(root);
        resultStack.push(new HashMap<>());

        while (!stack.isEmpty()) {
            Node currentNode = stack.pop();
            Map<String, Object> currentResult = resultStack.pop();

            // Add 'name' of the node
            currentResult.put("name", currentNode.getNodeName());

            // Add 'attributes'
            Map<String, String> attributesMap = new HashMap<>();
            if (currentNode.hasAttributes()) {
                NamedNodeMap attributes = currentNode.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node attr = attributes.item(i);
                    attributesMap.put(attr.getNodeName(), attr.getNodeValue());
                }
            }
            currentResult.put("attributes", attributesMap);

            // Variables to hold child nodes and text content
            List<Map<String, Object>> childrenList = new ArrayList<>();
            String textContent = null;

            NodeList nodeList = currentNode.getChildNodes();
            for (int i = nodeList.getLength() - 1; i >= 0; i--) {  // Traverse children
                Node childNode = nodeList.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    // Push child node and a new result map onto the stack
                    stack.push(childNode);
                    Map<String, Object> childResult = new HashMap<>();
                    childrenList.add(0, childResult);  // Add at the start to maintain order
                    resultStack.push(childResult);
                } else if (childNode.getNodeType() == Node.TEXT_NODE) {
                    // Capture text content
                    textContent = childNode.getTextContent().trim();
                }
            }

            // If text content exists, add as 'data'
            if (textContent != null && !textContent.isEmpty() && childrenList.isEmpty()) {
                currentResult.put("data", textContent);
            } else {
                currentResult.put("data", textContent != null ? textContent : "");
            }

            // Add 'children' if there are any child elements
            if (!childrenList.isEmpty()) {
                currentResult.put("children", childrenList);
            } else {
                currentResult.put("children", new ArrayList<>());  // Empty list for consistency
            }

            // If the node is the root, store the result in the final output map
            if (currentNode == root) {
                result.put("root", currentResult);
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
    return result;
}