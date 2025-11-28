package com.yoomie.web.services;

public interface CheckoutService {

    /**
     * Proses checkout untuk kasir tertentu:
     * - Ambil semua item cart milik cashierId
     * - Kurangi stok produk sesuai quantity
     *
     * @param cashierId ID kasir (user di aplikasi kasir)
     */
    void checkout(Long cashierId);
}
