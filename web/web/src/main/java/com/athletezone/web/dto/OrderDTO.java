package com.athletezone.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private Long userId;
    private String username; // Dari User
    private String createdOn; // Tanggal order
    private String cartSummary; // Ringkasan produk di cart
    private double totalAmount; // Total harga
    private String paymentMethod; // Metode pembayaran
    private String paymentStatus; // Status pembayaran
    private String address; // Alamat dari pembayaran
}
