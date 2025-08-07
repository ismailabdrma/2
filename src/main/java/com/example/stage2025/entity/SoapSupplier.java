package com.example.stage2025.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("SOAP")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SoapSupplier extends Supplier {
    private String wsdlUrl;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "soap_operation_meta_id", referencedColumnName = "id")
    private SoapSupplierOperationMeta soapOperationMeta;
}
