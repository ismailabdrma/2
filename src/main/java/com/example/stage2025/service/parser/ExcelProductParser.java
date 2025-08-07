package com.example.stage2025.service.parser;

import com.example.stage2025.dto.SupplierProductDto;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExcelProductParser implements ProductParser {

    @Override
    public List<SupplierProductDto> parse(MultipartFile file, String sheetName) {
        List<SupplierProductDto> products = new ArrayList<>();
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = sheetName != null && !sheetName.isEmpty() ? workbook.getSheet(sheetName) : workbook.getSheetAt(0);

            if (sheet == null) {
                throw new IllegalArgumentException("Sheet '" + sheetName + "' not found in the Excel file.");
            }

            DataFormatter dataFormatter = new DataFormatter();
            boolean firstRow = true;

            for (Row row : sheet) {
                if (firstRow) {
                    firstRow = false; // Skip header row
                    continue;
                }

                // Assuming columns: 0:externalProductId, 1:name, 2:description, 3:price, 4:stock, 5:category, 6:imageUrls (optional)
                String externalProductId = dataFormatter.formatCellValue(row.getCell(0));
                String name = dataFormatter.formatCellValue(row.getCell(1));
                String description = dataFormatter.formatCellValue(row.getCell(2));
                BigDecimal price = new BigDecimal(dataFormatter.formatCellValue(row.getCell(3)));
                Integer stock = Integer.parseInt(dataFormatter.formatCellValue(row.getCell(4)));
                String categoryName = dataFormatter.formatCellValue(row.getCell(5));
                List<String> imageUrls = new ArrayList<>();
                if (row.getCell(6) != null) {
                    String imageUrlsString = dataFormatter.formatCellValue(row.getCell(6));
                    if (imageUrlsString != null && !imageUrlsString.isEmpty()) {
                        imageUrls = Arrays.stream(imageUrlsString.split(";")) // Assuming image URLs are semicolon-separated
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList());
                    }
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
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage(), e);
        }
        return products;
    }
}
