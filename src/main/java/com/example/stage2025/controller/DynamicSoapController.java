package com.example.stage2025.controller;

import com.example.stage2025.dto.SoapFieldDto;
import com.example.stage2025.dto.SoapOperationMeta;
import com.example.stage2025.utils.SoapDynamicInvoker;
import com.example.stage2025.utils.WsdlFullMetadataUtils;
import com.example.stage2025.utils.WsdlOperationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/wsdl")
@PreAuthorize("hasRole('ADMIN')")
public class DynamicSoapController {

    private final SoapDynamicInvoker soapDynamicInvoker;

    @Autowired
    public DynamicSoapController(SoapDynamicInvoker soapDynamicInvoker) {
        this.soapDynamicInvoker = soapDynamicInvoker;
    }

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

    @PostMapping("/invoke")
    public ResponseEntity<String> invokeSoapOperation(
            @RequestParam String wsdlUrl,
            @RequestParam String operationName,
            @RequestParam(required = false) String soapAction,
            @RequestParam String inputElement,
            @RequestBody Map<String, String> inputParams) { // Dynamic input parameters
        try {
            String response = soapDynamicInvoker.invokeWebService(wsdlUrl, operationName, soapAction, inputElement, inputParams);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error invoking SOAP service: " + e.getMessage());
        }
    }
}
