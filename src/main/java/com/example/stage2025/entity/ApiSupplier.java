package com.example.stage2025.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("API")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ApiSupplier extends Supplier {
    private String apiUrl;
    // Potentially add apiKey, authMethod if needed for API suppliers
}
