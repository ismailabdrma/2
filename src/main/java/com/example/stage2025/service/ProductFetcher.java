package com.example.stage2025.service;

import com.example.stage2025.dto.SupplierProductDto;

import java.util.List;
import java.util.Map;

public interface ProductFetcher {
    List<SupplierProductDto> fetchProducts(String source, Map<String, String> params);
}
