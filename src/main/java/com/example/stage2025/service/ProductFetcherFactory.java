package com.example.stage2025.service;

import com.example.stage2025.enums.DataFormat;
import com.example.stage2025.service.impl.ApiProductFetcher;
import com.example.stage2025.service.impl.ExcelProductFetcher;
import com.example.stage2025.service.impl.CsvProductFetcher;
import com.example.stage2025.service.impl.SoapProductFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductFetcherFactory {

    private final ApiProductFetcher apiProductFetcher;
    private final ExcelProductFetcher excelProductFetcher;
    private final CsvProductFetcher csvProductFetcher;
    private final SoapProductFetcher soapProductFetcher;

    @Autowired
    public ProductFetcherFactory(ApiProductFetcher apiProductFetcher,
                                 ExcelProductFetcher excelProductFetcher,
                                 CsvProductFetcher csvProductFetcher,
                                 SoapProductFetcher soapProductFetcher) {
        this.apiProductFetcher = apiProductFetcher;
        this.excelProductFetcher = excelProductFetcher;
        this.csvProductFetcher = csvProductFetcher;
        this.soapProductFetcher = soapProductFetcher;
    }

    public ProductFetcher getFetcher(DataFormat dataFormat) {
        return switch (dataFormat) {
            case API -> apiProductFetcher;
            case EXCEL -> excelProductFetcher;
            case CSV -> csvProductFetcher;
            case SOAP -> soapProductFetcher;
            default -> throw new IllegalArgumentException("Unsupported data format: " + dataFormat);
        };
    }
}
