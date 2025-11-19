package com.yoomie.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private Long cashierId;
    private String cashierName; // Dari Cashier
    private String createdOn; // Tanggal transaction
    private String cartSummary; // Ringkasan produk di cart
    private double totalAmount; // Total harga
    private String paymentMethod; // Metode pembayaran
    private String paymentStatus; // Status pembayaran
    private String address; // Alamat dari pembayaran
}
