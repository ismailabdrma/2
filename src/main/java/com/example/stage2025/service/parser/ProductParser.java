package com.example.stage2025.service.parser;

import com.example.stage2025.dto.SupplierProductDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductParser {
    List<SupplierProductDto> parse(MultipartFile file, String param);
}
