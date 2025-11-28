package com.yoomie.web.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemSummaryDTO {
    // Nama produk
    private String itemName;

    // Total quantity terjual hari ini (akumulasi dari semua transaksi hari ini)
    private long itemSold;

    // Total sales untuk produk ini hari ini (qty * harga, diambil dari subTotal)
    private double sales;
}
