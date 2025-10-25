package com.athletezone.web.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payment_items")
public class PaymentItem extends BaseEntity {
    private String productName;
    private int quantity;
    private double price;
    private double subTotal;

    @ManyToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;
}
