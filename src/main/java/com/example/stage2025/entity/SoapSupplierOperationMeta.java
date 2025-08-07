package com.example.stage2025.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "soap_supplier_operation_meta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoapSupplierOperationMeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String operationName;

    private String soapAction;

    @Column(nullable = false)
    private String inputElement;

    @Column(nullable = false)
    private String outputElement;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "soap_input_fields", joinColumns = @JoinColumn(name = "operation_meta_id"))
    private List<SoapField> inputFields = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "soap_output_fields", joinColumns = @JoinColumn(name = "operation_meta_id"))
    private List<SoapField> outputFields = new ArrayList<>();

    // This would be more complex to store in a relational DB.
    // For simplicity, we might store it as JSON or handle it dynamically.
    // For now, let's assume complex types are resolved during WSDL parsing and not persisted here.
    // If needed, a separate table for complex types and their fields would be required.
}
