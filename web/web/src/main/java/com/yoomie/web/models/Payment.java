package com.yoomie.web.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payments")
public class Payment extends BaseEntity {
    private String paymentMethod;
    private double totalAmount;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    private List<PaymentItem> paymentItems;

    @ManyToOne
    @JoinColumn(name = "cashier_id")
    private Cashier cashier;
}
