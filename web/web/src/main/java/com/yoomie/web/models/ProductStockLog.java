package com.yoomie.web.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "product_stock_logs")
@JsonIgnoreProperties({"product"})
public class ProductStockLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer oldStock;

    @Column(nullable = false)
    private Integer newStock;

    @Column(nullable = false)
    private Integer diff;

    @Column(nullable = true)
    private Long adminId;
}
