package com.example.stage2025.service.impl;

import com.example.stage2025.dto.SupplierProductDto;
import com.example.stage2025.service.ProductFetcher;
import com.example.stage2025.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class ApiProductFetcher implements ProductFetcher {

    private final WebClient.Builder webClientBuilder;

    @Autowired
    public ApiProductFetcher(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public List<SupplierProductDto> fetchProducts(String apiUrl, Map<String, String> params) {
        // For simplicity, assuming the API returns a list of SupplierProductDto directly.
        // In a real scenario, you'd need more complex parsing based on the API's response structure.
        // You might also need to handle API keys, authentication, pagination, etc.

        // Example: If params contain query parameters for the API call
        WebClient.RequestHeadersUriSpec<?> request = webClientBuilder.baseUrl(apiUrl).build().get();
        if (params != null && !params.isEmpty()) {
            request.uri(uriBuilder -> {
                uriBuilder.path("/"); // Assuming base URL is the endpoint
                params.forEach(uriBuilder::queryParam);
                return uriBuilder.build();
            });
        }

        Mono<List<SupplierProductDto>> responseMono = request.retrieve()
                .bodyToMono(String.class) // Fetch as String first
                .map(json -> JsonUtils.parseJsonToList(json, SupplierProductDto.class)); // Then parse

        return responseMono.block(); // Block to get the result synchronously for now
    }
}
