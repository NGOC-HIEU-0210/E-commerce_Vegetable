package com.hieu.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hieu.enums.BatchStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Entity
@Table(name = "tbl_product_batch", indexes = {
        @Index(name = "idx_batch_product_expiry", columnList = "product_id, expiryDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    private Integer initialQuantity;

    private Integer remainingQuantity;


    private Double costPrice;

    private LocalDate receivedDate;

    private LocalDate expiryDate;


    private Integer wastedQuantity;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BatchStatus status;

    @PrePersist
    public void prePersist() {
        if (status == null) status = BatchStatus.ACTIVE;
        if (wastedQuantity == null) wastedQuantity = 0;
        if (remainingQuantity == null) remainingQuantity = initialQuantity;
    }
}
