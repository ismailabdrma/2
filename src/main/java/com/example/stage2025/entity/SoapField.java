package com.example.stage2025.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoapField {
    private String name;
    private String type;
    private boolean isComplex;
    private boolean isArray;
}
