package com.example.stage2025.service.parser;

import com.example.stage2025.dto.SupplierProductDto;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CsvProductParser implements ProductParser {

    @Override
    public List<SupplierProductDto> parse(MultipartFile file, String delimiter) {
        List<SupplierProductDto> products = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip header row
                    continue;
                }
                String[] parts = line.split(delimiter);
                if (parts.length >= 6) { // Assuming at least 6 columns: id, name, description, price, stock, category, imageUrls (optional)
                    try {
                        String externalProductId = parts[0].trim();
                        String name = parts[1].trim();
                        String description = parts[2].trim();
                        BigDecimal price = new BigDecimal(parts[3].trim());
                        Integer stock = Integer.parseInt(parts[4].trim());
                        String categoryName = parts[5].trim();
                        List<String> imageUrls = new ArrayList<>();
                        if (parts.length > 6 && parts[6] != null && !parts[6].trim().isEmpty()) {
                            imageUrls = Arrays.stream(parts[6].split(";")) // Assuming image URLs are semicolon-separated
                                    .map(String::trim)
                                    .filter(s -> !s.isEmpty())
                                    .collect(Collectors.toList());
                        }

                        products.add(SupplierProductDto.builder()
                                .externalProductId(externalProductId)
                                .name(name)
                                .description(description)
                                .price(price)
                                .stock(stock)
                                .categoryName(categoryName)
                                .imageUrls(imageUrls)
                                .build());
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping row due to number format error: " + line + " - " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Skipping row due to parsing error: " + line + " - " + e.getMessage());
                    }
                } else {
                    System.err.println("Skipping row due to insufficient columns: " + line);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage(), e);
        }
        return products;
    }
}
