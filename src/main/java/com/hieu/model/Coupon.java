package com.hieu.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tbl_coupon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** PERCENT hoặc FIXED */
    @Column(length = 20)
    private String discountType;

    private Double discountValue;

    private Double minOrderValue;

    private Double maxDiscount;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer usageLimit;

    private Integer usedCount;

    private Boolean active;

    @PrePersist
    public void prePersist() {
        if (active == null) active = true;
        if (usedCount == null) usedCount = 0;
        if (usageLimit == null) usageLimit = 0;
    }
}
