package com.example.stage2025.dto;

import com.example.stage2025.enums.DataFormat;
import com.example.stage2025.enums.PayoutFrequency;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierDto {
    private Long id;
    private String name;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private DataFormat dataFormat;
    private String apiUrl;
    private String wsdlUrl;
    private String excelSheetName;
    private String csvDelimiter;
    private PayoutFrequency payoutFrequency;
    private boolean active;
    private LocalDateTime lastImport;
    private SoapOperationMeta soapOperationMeta;
}
