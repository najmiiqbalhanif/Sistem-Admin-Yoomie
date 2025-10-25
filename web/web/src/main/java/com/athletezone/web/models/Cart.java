package com.athletezone.web.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "carts")
public class Cart extends BaseEntity {
    private double totalPrice;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @EqualsAndHashCode.Exclude // Mencegah infinite recursion
    @ToString.Exclude
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude // Mencegah infinite recursion
    @ToString.Exclude
    private List<CartItem> cartItems = new ArrayList<>();
}
