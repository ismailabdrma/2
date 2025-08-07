package com.example.stage2025.service;

import com.example.stage2025.dto.SoapFieldDto;
import com.example.stage2025.dto.SoapOperationMeta;
import com.example.stage2025.dto.SupplierProductDto;
import com.example.stage2025.entity.SoapSupplierOperationMeta;
import com.example.stage2025.repository.SoapSupplierOperationMetaRepository;
import com.example.stage2025.utils.SoapDynamicInvoker;
import com.example.stage2025.utils.WsdlFullMetadataUtils;
import com.example.stage2025.utils.WsdlOperationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SoapProductFetcher implements ProductFetcher {

    private final SoapDynamicInvoker soapDynamicInvoker;
    private final SoapSupplierOperationMetaRepository soapSupplierOperationMetaRepository;

    @Autowired
    public SoapProductFetcher(SoapDynamicInvoker soapDynamicInvoker,
                              SoapSupplierOperationMetaRepository soapSupplierOperationMetaRepository) {
        this.soapDynamicInvoker = soapDynamicInvoker;
        this.soapSupplierOperationMetaRepository = soapSupplierOperationMetaRepository;
    }

    @Override
    public List<SupplierProductDto> fetchProducts(String wsdlUrl, Map<String, String> params) {
        String operationName = params.get("operationName");
        String inputElement = params.get("inputElement");
        String outputElement = params.get("outputElement");
        String soapAction = params.get("soapAction");
        String inputFieldsJson = params.get("inputFields"); // JSON string of SoapFieldDto list

        if (operationName == null || inputElement == null || outputElement == null) {
            throw new IllegalArgumentException("Missing required SOAP parameters: operationName, inputElement, outputElement");
        }

        List<SoapFieldDto> inputFields = SoapFieldDto.fromJson(inputFieldsJson);
        Map<String, String> inputParams = inputFields.stream()
                .collect(Collectors.toMap(SoapFieldDto::getName, f -> params.get("soapInput_" + f.getName())));

        try {
            String soapResponse = soapDynamicInvoker.invokeWebService(wsdlUrl, operationName, soapAction, inputElement, inputParams);
            return parseSoapResponse(soapResponse, outputElement);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch products from SOAP supplier: " + e.getMessage(), e);
        }
    }

    private List<SupplierProductDto> parseSoapResponse(String soapResponse, String outputElement) throws Exception {
        List<SupplierProductDto> products = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // Important for SOAP XML
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(soapResponse.getBytes()));

        // Find the main output element, e.g., "GetProductsResponse" or "ProductList"
        NodeList outputNodes = doc.getElementsByTagNameNS("*", outputElement);
        if (outputNodes.getLength() == 0) {
            throw new RuntimeException("Could not find output element: " + outputElement + " in SOAP response.");
        }

        Element outputRoot = (Element) outputNodes.item(0);

        // Assuming products are direct children or nested within a "Product" tag
        NodeList productNodes = outputRoot.getElementsByTagNameNS("*", "Product"); // Common convention
        if (productNodes.getLength() == 0) {
            // Fallback: try to parse directly from outputRoot if it represents a single product or list of fields
            // This part might need to be more dynamic based on actual WSDL output structure
            // For now, assume "Product" is the repeating element.
            // If the output element itself is the product, handle it.
            if (outputRoot.getTagName().equals(outputElement)) {
                // If outputElement is the product itself, try to parse it as a single product
                try {
                    SupplierProductDto singleProduct = parseProductElement(outputRoot);
                    products.add(singleProduct);
                } catch (Exception e) {
                    // If it's not a single product, then it's likely a list of products under a different tag
                    // or the structure is more complex. Log and continue.
                    System.err.println("Warning: Output element '" + outputElement + "' is not a repeating 'Product' element and could not be parsed as a single product. Attempting to find nested products.");
                }
            }
        }


        for (int i = 0; i < productNodes.getLength(); i++) {
            Element productElement = (Element) productNodes.item(i);
            products.add(parseProductElement(productElement));
        }

        return products;
    }

    private SupplierProductDto parseProductElement(Element productElement) {
        // This is a generic parsing logic. You might need to customize it based on
        // the actual structure of the product data returned by the SOAP service.
        // It assumes common field names like "id", "name", "price", "stock", "category", "imageUrl".

        String externalProductId = getElementTextContent(productElement, "id");
        String name = getElementTextContent(productElement, "name");
        String description = getElementTextContent(productElement, "description");
        BigDecimal price = new BigDecimal(getElementTextContent(productElement, "price", "0.00"));
        Integer stock = Integer.parseInt(getElementTextContent(productElement, "stock", "0"));
        String categoryName = getElementTextContent(productElement, "category");
        List<String> imageUrls = new ArrayList<>();
        NodeList imageUrlNodes = productElement.getElementsByTagNameNS("*", "imageUrl");
        for (int i = 0; i < imageUrlNodes.getLength(); i++) {
            imageUrls.add(imageUrlNodes.item(i).getTextContent());
        }
        if (imageUrls.isEmpty()) {
            // Fallback for single image URL field
            String singleImageUrl = getElementTextContent(productElement, "image");
            if (singleImageUrl != null && !singleImageUrl.isEmpty()) {
                imageUrls.add(singleImageUrl);
            }
        }


        return SupplierProductDto.builder()
                .externalProductId(externalProductId)
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .categoryName(categoryName)
                .imageUrls(imageUrls)
                .build();
    }

    private String getElementTextContent(Element parentElement, String tagName) {
        NodeList nodeList = parentElement.getElementsByTagNameNS("*", tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    private String getElementTextContent(Element parentElement, String tagName, String defaultValue) {
        String content = getElementTextContent(parentElement, tagName);
        return content != null && !content.isEmpty() ? content : defaultValue;
    }
}
