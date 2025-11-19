package com.yoomie.web.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "transactions")
public class Transaction extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "cashier_id", nullable = false)
    private Cashier cashier;

    @OneToOne
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;
}
