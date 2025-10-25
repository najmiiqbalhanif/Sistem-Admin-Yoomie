package com.athletezone.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long id;
    private int quantity;
    private double price;
    private double subTotal;
    private String name;
    private String photoUrl;
    private String category;
    private String brand;
    private ProductDTO product;
}