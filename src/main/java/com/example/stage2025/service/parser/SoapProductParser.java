package com.example.stage2025.service.parser;

import com.example.stage2025.dto.SupplierProductDto;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class SoapProductParser implements ProductParser {

    @Override
    public List<SupplierProductDto> parse(MultipartFile file, String param) {
        // This parser is for local WSDL/XML files if you were to upload them.
        // For dynamic SOAP calls, the SoapProductFetcher is used.
        // This implementation would involve parsing the XML content of the file
        // based on a known SOAP response structure.
        throw new UnsupportedOperationException("Parsing SOAP XML from file is not yet implemented.");
    }
}
