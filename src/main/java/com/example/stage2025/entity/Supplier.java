package com.example.stage2025.entity;

import com.example.stage2025.enums.DataFormat;
import com.example.stage2025.enums.PayoutFrequency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "data_format", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String contactEmail;

    private String contactPhone;
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_format", insertable = false, updatable = false)
    private DataFormat dataFormat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayoutFrequency payoutFrequency;

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime lastImport;

    @PrePersist
    protected void onCreate() {
        if (dataFormat == null) {
            // This should be set by the subclass before persist
            // Or handled by a custom persist listener
        }
    }
}
