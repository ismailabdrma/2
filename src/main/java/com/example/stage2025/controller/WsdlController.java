package com.example.stage2025.controller;

import com.example.stage2025.dto.SoapOperationMeta;
import com.example.stage2025.utils.WsdlFullMetadataUtils;
import com.example.stage2025.utils.WsdlOperationUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/wsdl")
@PreAuthorize("hasRole('ADMIN')")
public class WsdlController {

    @GetMapping("/operations")
    public ResponseEntity<List<SoapOperationMeta>> getWsdlOperations(@RequestParam String wsdlUrl) {
        try {
            List<SoapOperationMeta> operations = WsdlOperationUtils.getOperations(wsdlUrl);
            return ResponseEntity.ok(operations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null); // Or a more detailed error response
        }
    }

    @GetMapping("/operation-metadata")
    public ResponseEntity<SoapOperationMeta> getWsdlOperationMetadata(
            @RequestParam String wsdlUrl,
            @RequestParam String operationName) {
        try {
            SoapOperationMeta metadata = WsdlFullMetadataUtils.getOperationMetadata(wsdlUrl, operationName);
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null); // Or a more detailed error response
        }
    }
}
