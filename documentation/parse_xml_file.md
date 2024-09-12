## Documentation: `parse_xml_file`

---

### Usage

#### Imports Requried

```java
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
```


#### Function

```java
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
```

#### Example Usage
```java
import java.io.File;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Get a file
        File xml_file = new File("test.xml");

        // Parse xml file
        Map<String, Object> results = parser.parse_xml_file(xmlFile);

        // Print the results
        System.out.println(results);
    }

    /**
     * Include the parse_xml_file function here
     */
    public Map<String, Object> parse_xml_file(File file) {
        // (Copy the function implementation from above)
    }
}

```
---

### Function Overview

The `parse_xml_file` function reads an XML file, processes its structure, and returns a nested data structure (in the form of a `Map<String, Object>`) that contains details about each XML element, including:

- The name of the element
- Text content (if any)
- Attributes (if any)
- Children elements (if any)

### **Return Format**

The function returns a `Map<String, Object>` where each XML element is represented as a dictionary-like structure with the following keys:

- **`name`**: The tag name of the XML element.
- **`data`**: The text content inside the element. If there is no text content, it is an empty string (`""`).
- **`attributes`**: A map containing the attributes of the XML element. If there are no attributes, it is an empty map (`{}`).
- **`children`**: A list of child elements (each represented as a `Map`). If the element has no children, it is an empty list (`[]`).

---

### Example XML Input

The following is an example XML file structure that the `parse_xml_file` function can process:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<root>
    <testCase id="1">
        <rateOfTransmission unit="gb/s">1</rateOfTransmission>
        <numberOfPackets>10000</numberOfPackets>
    </testCase>
    <testCase id="2">
        <rateOfTransmission unit="gb/s">10</rateOfTransmission>
        <numberOfPackets>1000</numberOfPackets>
    </testCase>
</root>
```

---

### Example Output Structure

The function returns the following data structure in `Map<String, Object>` format for the given XML input:

```json
{
    "root": {
        "name": "root",
        "data": "",
        "attributes": {},
        "children": [
            {
                "name": "testCase",
                "data": "",
                "attributes": {
                    "id": "1"
                },
                "children": [
                    {
                        "name": "rateOfTransmission",
                        "data": "1",
                        "attributes": {
                            "unit": "gb/s"
                        },
                        "children": []
                    },
                    {
                        "name": "numberOfPackets",
                        "data": "10000",
                        "attributes": {},
                        "children": []
                    }
                ]
            },
            {
                "name": "testCase",
                "data": "",
                "attributes": {
                    "id": "2"
                },
                "children": [
                    {
                        "name": "rateOfTransmission",
                        "data": "10",
                        "attributes": {
                            "unit": "gb/s"
                        },
                        "children": []
                    },
                    {
                        "name": "numberOfPackets",
                        "data": "1000",
                        "attributes": {},
                        "children": []
                    }
                ]
            }
        ]
    }
}
```

---

### Output Structure in Detail

Each element in the output follows this structure:

```json
{
    "name": "element_name",
    "data": "element_text_content",
    "attributes": {
        "attribute_name": "attribute_value"
    },
    "children": [
        // List of child elements following the same structure
    ]
}
```

---

### Special Cases

- **Text Nodes**: If an element contains text, it will be placed under the `"data"` key. If an element has no text, `"data"` will be an empty string.
- **Attributes**: If an element has attributes, they will be stored in the `"attributes"` map. If there are no attributes, it will be an empty map.
- **Children**: If an element has child elements, they will be stored in the `"children"` list. If there are no children, it will be an empty list.

---