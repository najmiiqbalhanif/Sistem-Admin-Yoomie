package com.athletezone.web.models;

import lombok.*;
import jakarta.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    private String name;
    private String brand;
    private String category;
    private String photoUrl;
    private double price;
    private int stock;

    // Relasi ke CartItem
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems;
}
