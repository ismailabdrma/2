package com.example.stage2025.service.parser;

import com.example.stage2025.dto.SupplierProductDto;
import com.example.stage2025.utils.JsonUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Component
public class JsonProductParser implements ProductParser {

    @Override
    public List<SupplierProductDto> parse(MultipartFile file, String param) {
        try {
            String jsonContent = new String(file.getBytes());
            // Assuming the JSON file contains a list of SupplierProductDto objects
            return JsonUtils.parseJsonToList(jsonContent, SupplierProductDto.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON file: " + e.getMessage(), e);
        }
    }
}
