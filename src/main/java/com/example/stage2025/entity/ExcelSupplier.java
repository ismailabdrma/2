package com.example.stage2025.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("EXCEL")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ExcelSupplier extends Supplier {
    private String excelSheetName;
    // Potentially add filePath or storage location for Excel files
}
