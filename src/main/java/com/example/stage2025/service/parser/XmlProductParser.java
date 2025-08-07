package com.example.stage2025.service.parser;

import com.example.stage2025.dto.SupplierProductDto;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class XmlProductParser implements ProductParser {

    @Override
    public List<SupplierProductDto> parse(MultipartFile file, String rootElement) {
        List<SupplierProductDto> products = new ArrayList<>();
        try (InputStream is = file.getInputStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList productNodes = doc.getElementsByTagName(rootElement); // e.g., "Product" or "Item"

            for (int i = 0; i < productNodes.getLength(); i++) {
                Element productElement = (Element) productNodes.item(i);
                products.add(parseProductElement(productElement));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse XML file: " + e.getMessage(), e);
        }
        return products;
    }

    private SupplierProductDto parseProductElement(Element productElement) {
        // This is a generic parsing logic. You might need to customize it based on
        // the actual structure of the product data in the XML file.
        // It assumes common field names like "id", "name", "price", "stock", "category", "imageUrl".

        String externalProductId = getElementTextContent(productElement, "externalProductId");
        String name = getElementTextContent(productElement, "name");
        String description = getElementTextContent(productElement, "description");
        BigDecimal price = new BigDecimal(getElementTextContent(productElement, "price", "0.00"));
        Integer stock = Integer.parseInt(getElementTextContent(productElement, "stock", "0"));
        String categoryName = getElementTextContent(productElement, "categoryName");
        List<String> imageUrls = new ArrayList<>();
        NodeList imageUrlNodes = productElement.getElementsByTagName("imageUrls");
        if (imageUrlNodes.getLength() > 0) {
            String urlsString = imageUrlNodes.item(0).getTextContent();
            if (urlsString != null && !urlsString.isEmpty()) {
                imageUrls = Arrays.stream(urlsString.split(";")) // Assuming image URLs are semicolon-separated
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            }
        } else {
            // Fallback for single image URL field
            String singleImageUrl = getElementTextContent(productElement, "imageUrl");
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
        NodeList nodeList = parentElement.getElementsByTagName(tagName);
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
