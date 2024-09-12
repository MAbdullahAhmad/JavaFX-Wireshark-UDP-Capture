# Import necessary library again since context might have been lost
import xml.etree.ElementTree as ET

# Recreate the XML data and write the files again

# Data from Image 1
data_image_1 = [
    {"TestCaseId": "1", "rateOfTransmission": "1gb/s", "numberOfPacket": "10,000"},
    {"TestCaseId": "2", "rateOfTransmission": "10gb/s", "numberOfPacket": "1,000"}
]

# Create root element for XML
root_1 = ET.Element("TestCases")

# Add each row as a TestCase element
for row in data_image_1:
    test_case = ET.SubElement(root_1, "TestCase")
    for key, value in row.items():
        child = ET.SubElement(test_case, key)
        child.text = value

# Generate XML string for Image 1
xml_data_1 = ET.tostring(root_1, encoding='unicode', method='xml')

# Write XML to file
with open("/mnt/data/image_1.xml", "w") as file:
    file.write(xml_data_1)


# Data from Image 2
data_image_2 = [
    {"MSGDataID": "23456", "MSGID": "1001", "850": "25", "955": "85.5", "956": "25", "957": "85.5", "960": "85.5", "961": "97.6", "962": "109.7", "955_again": "121.8", "956_again": "133.9", "957_again": "146"},
    {"MSGDataID": "58", "MSGID": "1001", "850": "25", "955": "85.5", "956": "25", "957": "85.5", "960": "85.5", "961": "97.6", "962": "85.5", "955_again": "85.5", "956_again": "97.6", "957_again": "88"}
]

# Create root element for XML
root_2 = ET.Element("Messages")

# Add each row as a Message element
for row in data_image_2:
    message = ET.SubElement(root_2, "Message")
    for key, value in row.items():
        child = ET.SubElement(message, key)
        child.text = value

# Generate XML string for Image 2
xml_data_2 = ET.tostring(root_2, encoding='unicode', method='xml')

# Write XML to file
with open("/mnt/data/image_2.xml", "w") as file:
    file.write(xml_data_2)


# Data from Image 3
data_image_3 = [
    {"MSGDataID": "23456", "MSGId": "1001", "GFSMSource": "20001", "GFSMDestination": "20001", "DataId": "10001", "RuleId": "1001", "Source(MPR)": "20001", "Destination(MPR)": "10001", "destinationIp": "192.1.1.5", "sourceIp": "192.1.1.6", "destinationPort": "10000", "sourcePort": "10001", "ProtocolId": "17", "ExpectedResult": "allow"},
    {"MSGDataID": "524", "MSGId": "1001", "GFSMSource": "10002", "GFSMDestination": "10002", "DataId": "10001", "RuleId": "1002", "Source(MPR)": "10002", "Destination(MPR)": "10001", "destinationIp": "192.1.1.5", "sourceIp": "192.1.1.6", "destinationPort": "10000", "sourcePort": "10001", "ProtocolId": "17", "ExpectedResult": "Not allow"}
]

# Create root element for XML
root_3 = ET.Element("TestResults")

# Add each row as a TestResult element
for row in data_image_3:
    result = ET.SubElement(root_3, "TestResult")
    for key, value in row.items():
        child = ET.SubElement(result, key)
        child.text = value

# Generate XML string for Image 3
xml_data_3 = ET.tostring(root_3, encoding='unicode', method='xml')

# Write XML to file
with open("/mnt/data/image_3.xml", "w") as file:
    file.write(xml_data_3)

("/mnt/data/image_1.xml", "/mnt/data/image_2.xml", "/mnt/data/image_3.xml")
