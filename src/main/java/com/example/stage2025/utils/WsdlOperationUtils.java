package com.example.stage2025.utils;

import com.example.stage2025.dto.SoapOperationMeta;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPOperation;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class WsdlOperationUtils {

    public static List<SoapOperationMeta> getOperations(String wsdlUrl) throws WSDLException, IOException, SAXException, ParserConfigurationException {
        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
        reader.setFeature("javax.wsdl.importDocuments", true); // Resolve imports
        Definition definition = reader.readWSDL(wsdlUrl);

        List<SoapOperationMeta> operationsMeta = new ArrayList<>();
        String targetNamespace = definition.getTargetNamespace();

        for (Object portTypeObj : definition.getPortTypes().values()) {
            PortType portType = (PortType) portTypeObj;
            for (Object operationObj : portType.getOperations()) {
                Operation operation = (Operation) operationObj;
                SoapOperationMeta meta = new SoapOperationMeta();
                meta.setOperationName(operation.getName());

                // Get SOAP Action from binding if available
                definition.getBindings().values().stream()
                        .filter(binding -> binding.getPortType().equals(portType))
                        .flatMap(binding -> binding.getBindingOperations().stream())
                        .filter(bindingOp -> bindingOp.getName().equals(operation.getName()))
                        .findFirst()
                        .ifPresent(bindingOp -> bindingOp.getExtensibilityElements().stream()
                                .filter(ext -> ext instanceof SOAPOperation)
                                .map(ext -> (SOAPOperation) ext)
                                .findFirst()
                                .ifPresent(soapOp -> meta.setSoapAction(soapOp.getSoapActionURI())));

                // Input Message Element Name
                if (operation.getInput() != null && operation.getInput().getMessage() != null) {
                    Part inputPart = operation.getInput().getMessage().getPart("parameters"); // Common convention
                    if (inputPart != null && inputPart.getElementName() != null) {
                        meta.setInputElement(inputPart.getElementName().getLocalPart());
                    }
                }

                // Output Message Element Name
                if (operation.getOutput() != null && operation.getOutput().getMessage() != null) {
                    Part outputPart = operation.getOutput().getMessage().getPart("parameters"); // Common convention
                    if (outputPart != null && outputPart.getElementName() != null) {
                        meta.setOutputElement(outputPart.getElementName().getLocalPart());
                    }
                }
                operationsMeta.add(meta);
            }
        }
        return operationsMeta;
    }

    public static String getTargetNamespace(String wsdlUrl) throws WSDLException {
        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
        reader.setFeature("javax.wsdl.importDocuments", true);
        Definition definition = reader.readWSDL(wsdlUrl);
        return definition.getTargetNamespace();
    }
}
