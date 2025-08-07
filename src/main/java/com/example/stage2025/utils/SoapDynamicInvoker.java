package com.example.stage2025.utils;

import com.example.stage2025.dto.SoapOperationMeta;
import com.example.stage2025.dto.SoapFieldDto;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

@Component
public class SoapDynamicInvoker {

    public static Map<String, Object> call(
            String endpointUrl,
            String namespace,
            SoapOperationMeta operationMeta,
            Map<String, Object> inputValues,
            Map<String, List<SoapFieldDto>> complexTypes
    ) throws Exception {

        // 1. Build request
        SOAPMessage request = createSoapRequest(namespace, operationMeta, inputValues, complexTypes);

        // 2. Send request
        SOAPMessage response = sendSoapRequest(endpointUrl, operationMeta.getSoapAction(), request);

        // 3. Process response
        return parseSoapResponse(response, operationMeta, complexTypes);
    }

    private static SOAPMessage createSoapRequest(
            String namespace,
            SoapOperationMeta operationMeta,
            Map<String, Object> inputValues,
            Map<String, List<SoapFieldDto>> complexTypes
    ) throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage request = factory.createMessage();
        SOAPPart part = request.getSOAPPart();
        SOAPEnvelope envelope = part.getEnvelope();
        SOAPBody body = envelope.getBody();

        // Add namespace declaration
        envelope.addNamespaceDeclaration("ws", namespace);

        // Create body element
        QName bodyQName = new QName(namespace, operationMeta.getInputElement(), "ws");
        SOAPBodyElement bodyElement = body.addBodyElement(bodyQName);

        // Add input fields
        for (SoapFieldDto field : operationMeta.getInputFields()) {
            Object value = inputValues.get(field.getName());
            if (value != null) {
                addSoapField(bodyElement, field, value, envelope, namespace, complexTypes);
            }
        }

        request.saveChanges();
        return request;
    }

    private static SOAPMessage sendSoapRequest(
            String endpointUrl,
            String soapAction,
            SOAPMessage request
    ) throws Exception {
        SOAPConnection conn = SOAPConnectionFactory.newInstance().createConnection();
        try {
            // Set SOAPAction header
            MimeHeaders headers = request.getMimeHeaders();
            if (soapAction != null && !soapAction.isEmpty()) {
                headers.setHeader("SOAPAction", soapAction);
            }

            // Send request
            return conn.call(request, endpointUrl);
        } finally {
            conn.close();
        }
    }

    private static Map<String, Object> parseSoapResponse(
            SOAPMessage response,
            SoapOperationMeta operationMeta,
            Map<String, List<SoapFieldDto>> complexTypes
    ) throws Exception {
        // Convert to XML string for parsing
        String xml = soapMessageToString(response);
        // System.out.println("SOAP Response XML:\n" + xml); // Removed for production

        // Parse XML document with namespace awareness
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);  // Critical fix
        Document doc = dbf.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes()));

        // Find response element
        Element responseRoot = findElementByLocalName(doc, operationMeta.getOutputElement());
        if (responseRoot == null) return Collections.emptyMap();

        return parseResponseBody(responseRoot, operationMeta.getOutputFields(), complexTypes);
    }

    private static Map<String, Object> parseResponseBody(
            Element responseRoot,
            List<SoapFieldDto> outputFields,
            Map<String, List<SoapFieldDto>> complexTypes
    ) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (SoapFieldDto field : outputFields) {
            // Find elements ignoring namespace
            List<Element> elements = findElementsByLocalName(responseRoot, field.getName());

            if (elements.isEmpty()) continue;

            // Handle products field
            if ("products".equals(field.getName())) {
                List<Object> products = new ArrayList<>();
                for (Element element : elements) {
                    // Look for product elements inside products
                    List<Element> productElements = findElementsByLocalName(element, "product");
                    if (!productElements.isEmpty()) {
                        for (Element productEl : productElements) {
                            products.add(parseComplexElement(productEl, "product", complexTypes));
                        }
                    }
                    // Handle flat structure
                    else {
                        products.add(parseComplexElement(element, "product", complexTypes));
                    }
                }
                result.put(field.getName(), products);
            }
            // Handle other array fields
            else if (elements.size() > 1) {
                List<Object> items = new ArrayList<>();
                for (Element element : elements) {
                    items.add(parseElementValue(element, field, complexTypes));
                }
                result.put(field.getName(), items);
            }
            // Single element
            else {
                result.put(field.getName(), parseElementValue(elements.get(0), field, complexTypes));
            }
        }
        return result;
    }

    private static Object parseElementValue(
            Element element,
            SoapFieldDto field,
            Map<String, List<SoapFieldDto>> complexTypes
    ) {
        if (isComplexType(field.getType(), complexTypes)) {
            return parseComplexElement(element, field.getType(), complexTypes);
        }
        return element.getTextContent();
    }

    private static void addSoapField(
            SOAPElement parent,
            SoapFieldDto field,
            Object value,
            SOAPEnvelope envelope,
            String namespace,
            Map<String, List<SoapFieldDto>> complexTypes
    ) throws SOAPException {
        QName fieldQName = new QName(namespace, field.getName(), "ws");

        if (isComplexType(field.getType(), complexTypes)) {
            SOAPElement complex = parent.addChildElement(fieldQName);
            @SuppressWarnings("unchecked")
            Map<String, Object> subValues = (Map<String, Object>) value;
            for (SoapFieldDto subField : complexTypes.get(getSimpleType(field.getType()))) {
                Object subValue = subValues.get(subField.getName());
                if (subValue != null) {
                    addSoapField(complex, subField, subValue, envelope, namespace, complexTypes);
                }
            }
        } else if (value instanceof List) {
            // Handle arrays
            for (Object item : (List<?>) value) {
                SOAPElement arrayElement = parent.addChildElement(fieldQName);
                arrayElement.addTextNode(item.toString());
            }
        } else {
            parent.addChildElement(fieldQName).addTextNode(value.toString());
        }
    }

    // --- Helper methods ---

    private static boolean isComplexType(String type, Map<String, List<SoapFieldDto>> complexTypes) {
        return type != null && complexTypes.containsKey(getSimpleType(type));
    }

    private static String getSimpleType(String type) {
        return type.contains(":") ? type.split(":")[1] : type;
    }

    private static Map<String, Object> parseComplexElement(
            Element el,
            String type,
            Map<String, List<SoapFieldDto>> complexTypes
    ) {
        Map<String, Object> map = new HashMap<>();
        String typeName = getSimpleType(type);
        List<SoapFieldDto> fields = complexTypes.get(typeName);

        if (fields == null) return map;

        for (SoapFieldDto field : fields) {
            List<Element> elements = findElementsByLocalName(el, field.getName());
            if (!elements.isEmpty()) {
                if (elements.size() > 1) {
                    List<Object> items = new ArrayList<>();
                    for (Element element : elements) {
                        items.add(parseElementValue(element, field, complexTypes));
                    }
                    map.put(field.getName(), items);
                } else {
                    map.put(field.getName(), parseElementValue(elements.get(0), field, complexTypes));
                }
            }
        }
        return map;
    }

    private static Element findElementByLocalName(Document doc, String localName) {
        return findElementsByLocalName(doc.getDocumentElement(), localName)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private static List<Element> findElementsByLocalName(Element parent, String localName) {
        List<Element> elements = new ArrayList<>();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = getNodeLocalName(node);
                if (localName.equals(nodeName)) {
                    elements.add((Element) node);
                }
                // Recursively search children
                elements.addAll(findElementsByLocalName((Element) node, localName));
            }
        }
        return elements;
    }

    // Safe method to get local name
    private static String getNodeLocalName(Node node) {
        String localName = node.getLocalName();
        if (localName != null) return localName;

        // Fallback for non-namespace aware nodes
        String name = node.getNodeName();
        int colonPos = name.indexOf(':');
        return (colonPos > -1) ? name.substring(colonPos + 1) : name;
    }

    private static String soapMessageToString(SOAPMessage message) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        StringWriter writer = new StringWriter();
        transformer.transform(
                new DOMSource(message.getSOAPPart()),
                new StreamResult(writer)
        );
        return writer.toString();
    }

    public String invokeWebService(String wsdlUrl, String operationName, String soapAction, String inputElement, Map<String, String> inputParams) throws Exception {
        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection soapConnection = soapConnectionFactory.createConnection();

        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();

        // SOAP Envelope and Body
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("tns", WsdlOperationUtils.getTargetNamespace(wsdlUrl)); // Use actual target namespace

        SOAPBody soapBody = envelope.getBody();
        Name bodyName = envelope.createName(inputElement, "tns", WsdlOperationUtils.getTargetNamespace(wsdlUrl));
        SOAPBodyElement bodyElement = soapBody.addBodyElement(bodyName);

        // Add input parameters dynamically
        for (Map.Entry<String, String> entry : inputParams.entrySet()) {
            Name paramName = envelope.createName(entry.getKey());
            SOAPElement paramElement = bodyElement.addChildElement(paramName);
            paramElement.setValue(entry.getValue());
        }

        soapMessage.saveChanges();

        // Add SOAPAction header if provided
        if (soapAction != null && !soapAction.isEmpty()) {
            MimeHeaders headers = soapMessage.getMimeHeaders();
            headers.addHeader("SOAPAction", soapAction);
        }

        soapConnection.close();

        return convertSOAPMessageToString(soapMessage);
    }

    private String convertSOAPMessageToString(SOAPMessage soapMessage) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        soapMessage.writeTo(baos);
        String xmlString = baos.toString("UTF-8");

        // Pretty print the XML
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString();
    }
}
