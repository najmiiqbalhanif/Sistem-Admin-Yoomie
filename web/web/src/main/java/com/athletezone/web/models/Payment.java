package com.athletezone.web.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payments")
public class Payment extends BaseEntity {
    private String paymentMethod;
    private String address;
    private double totalAmount;

    @Builder.Default
    private String status = "paid";

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    private List<PaymentItem> paymentItems;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
