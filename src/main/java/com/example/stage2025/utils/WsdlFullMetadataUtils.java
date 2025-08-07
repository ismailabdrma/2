package com.example.stage2025.utils;

import com.example.stage2025.dto.SoapFieldDto;
import com.example.stage2025.dto.SoapOperationMeta;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WsdlFullMetadataUtils {

    public static SoapOperationMeta getOperationMetadata(String wsdlUrl, String operationName) throws WSDLException, IOException, SAXException, ParserConfigurationException {
        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
        reader.setFeature("javax.wsdl.importDocuments", true);
        Definition definition = reader.readWSDL(wsdlUrl);

        String targetNamespace = definition.getTargetNamespace();

        for (Object portTypeObj : definition.getPortTypes().values()) {
            PortType portType = (PortType) portTypeObj;
            for (Object operationObj : portType.getOperations()) {
                Operation operation = (Operation) operationObj;
                if (operation.getName().equals(operationName)) {
                    SoapOperationMeta meta = new SoapOperationMeta();
                    meta.setOperationName(operation.getName());

                    // Get SOAP Action from binding if available
                    definition.getBindings().values().stream()
                            .filter(binding -> binding.getPortType().equals(portType))
                            .flatMap(binding -> binding.getBindingOperations().stream())
                            .filter(bindingOp -> bindingOp.getName().equals(operation.getName()))
                            .findFirst()
                            .ifPresent(bindingOp -> bindingOp.getExtensibilityElements().stream()
                                    .filter(ext -> ext instanceof javax.wsdl.extensions.soap.SOAPOperation)
                                    .map(ext -> (javax.wsdl.extensions.soap.SOAPOperation) ext)
                                    .findFirst()
                                    .ifPresent(soapOp -> meta.setSoapAction(soapOp.getSoapActionURI())));


                    // Input Message
                    if (operation.getInput() != null) {
                        Part inputPart = operation.getInput().getMessage().getPart("parameters"); // Common convention
                        if (inputPart != null && inputPart.getElementName() != null) {
                            meta.setInputElement(inputPart.getElementName().getLocalPart());
                            meta.setInputFields(extractFieldsFromElement(definition, inputPart.getElementName(), targetNamespace));
                        }
                    }

                    // Output Message
                    if (operation.getOutput() != null) {
                        Part outputPart = operation.getOutput().getMessage().getPart("parameters"); // Common convention
                        if (outputPart != null && outputPart.getElementName() != null) {
                            meta.setOutputElement(outputPart.getElementName().getLocalPart());
                            meta.setOutputFields(extractFieldsFromElement(definition, outputPart.getElementName(), targetNamespace));
                        }
                    }

                    meta.setComplexTypes(extractAllComplexTypes(definition, targetNamespace));

                    return meta;
                }
            }
        }
        throw new IllegalArgumentException("Operation " + operationName + " not found in WSDL.");
    }

    private static List<SoapFieldDto> extractFieldsFromElement(Definition definition, QName elementName, String targetNamespace) throws ParserConfigurationException, IOException, SAXException {
        List<SoapFieldDto> fields = new ArrayList<>();
        Types types = definition.getTypes();
        if (types == null || types.getExtensibilityElements().isEmpty()) {
            return Collections.emptyList();
        }

        for (Object ext : types.getExtensibilityElements()) {
            if (ext instanceof Schema) {
                Schema schema = (Schema) ext;
                Element schemaElement = schema.getElement();
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new InputSource(new StringReader(elementToString(schemaElement))));

                NodeList elements = doc.getElementsByTagNameNS(targetNamespace, "element");
                for (int i = 0; i < elements.getLength(); i++) {
                    Element element = (Element) elements.item(i);
                    if (element.getAttribute("name").equals(elementName.getLocalPart())) {
                        NodeList sequence = element.getElementsByTagNameNS(targetNamespace, "sequence");
                        if (sequence.getLength() > 0) {
                            Element sequenceElement = (Element) sequence.item(0);
                            NodeList childElements = sequenceElement.getElementsByTagNameNS(targetNamespace, "element");
                            for (int j = 0; j < childElements.getLength(); j++) {
                                Element childElement = (Element) childElements.item(j);
                                String name = childElement.getAttribute("name");
                                String type = childElement.getAttribute("type");
                                String ref = childElement.getAttribute("ref"); // For referenced types

                                if (name.isEmpty() && !ref.isEmpty()) {
                                    // If it's a ref, find the actual element definition
                                    Element refElement = findElementByName(doc, ref, targetNamespace);
                                    if (refElement != null) {
                                        name = refElement.getAttribute("name");
                                        type = refElement.getAttribute("type");
                                        if (type.isEmpty()) { // If ref element itself is a complex type
                                            type = ref; // Use ref name as type
                                        }
                                    }
                                }

                                if (!name.isEmpty()) {
                                    boolean isComplex = type.startsWith("tns:") || type.contains(":"); // Check if it's a custom complex type
                                    boolean isArray = childElement.getAttribute("maxOccurs").equals("unbounded");
                                    fields.add(new SoapFieldDto(name, type, isComplex, isArray));
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        return fields;
    }

    private static Map<String, List<SoapFieldDto>> extractAllComplexTypes(Definition definition, String targetNamespace) throws ParserConfigurationException, IOException, SAXException {
        Map<String, List<SoapFieldDto>> complexTypes = new HashMap<>();
        Types types = definition.getTypes();
        if (types == null || types.getExtensibilityElements().isEmpty()) {
            return Collections.emptyMap();
        }

        for (Object ext : types.getExtensibilityElements()) {
            if (ext instanceof Schema) {
                Schema schema = (Schema) ext;
                Element schemaElement = schema.getElement();
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new InputSource(new StringReader(elementToString(schemaElement))));

                NodeList complexTypeNodes = doc.getElementsByTagNameNS(targetNamespace, "complexType");
                for (int i = 0; i < complexTypeNodes.getLength(); i++) {
                    Element complexTypeElement = (Element) complexTypeNodes.item(i);
                    String typeName = complexTypeElement.getAttribute("name");
                    if (!typeName.isEmpty()) {
                        List<SoapFieldDto> fields = new ArrayList<>();
                        NodeList sequence = complexTypeElement.getElementsByTagNameNS(targetNamespace, "sequence");
                        if (sequence.getLength() > 0) {
                            Element sequenceElement = (Element) sequence.item(0);
                            NodeList childElements = sequenceElement.getElementsByTagNameNS(targetNamespace, "element");
                            for (int j = 0; j < childElements.getLength(); j++) {
                                Element childElement = (Element) childElements.item(j);
                                String name = childElement.getAttribute("name");
                                String type = childElement.getAttribute("type");
                                String ref = childElement.getAttribute("ref");

                                if (name.isEmpty() && !ref.isEmpty()) {
                                    Element refElement = findElementByName(doc, ref, targetNamespace);
                                    if (refElement != null) {
                                        name = refElement.getAttribute("name");
                                        type = refElement.getAttribute("type");
                                        if (type.isEmpty()) {
                                            type = ref;
                                        }
                                    }
                                }

                                if (!name.isEmpty()) {
                                    boolean isComplex = type.startsWith("tns:") || type.contains(":");
                                    boolean isArray = childElement.getAttribute("maxOccurs").equals("unbounded");
                                    fields.add(new SoapFieldDto(name, type, isComplex, isArray));
                                }
                            }
                        }
                        complexTypes.put(typeName, fields);
                    }
                }
            }
        }
        return complexTypes;
    }

    private static Element findElementByName(Document doc, String name, String namespaceURI) {
        NodeList elements = doc.getElementsByTagNameNS(namespaceURI, "element");
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            if (element.getAttribute("name").equals(name)) {
                return element;
            }
        }
        return null;
    }

    private static String elementToString(Element element) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(element), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (Exception e) {
            throw new RuntimeException("Error converting element to string", e);
        }
    }
}
