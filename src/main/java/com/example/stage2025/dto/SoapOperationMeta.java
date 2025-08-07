package com.example.stage2025.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SoapOperationMeta {
    private String operationName;
    private String soapAction;
    private String inputElement;
    private String outputElement;
    private List<SoapFieldDto> inputFields;
    private List<SoapFieldDto> outputFields;
    private Map<String, List<SoapFieldDto>> complexTypes; // Map of complex type name to its fields
}
