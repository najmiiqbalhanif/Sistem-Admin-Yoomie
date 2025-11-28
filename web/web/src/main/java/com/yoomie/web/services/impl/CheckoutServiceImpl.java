package com.yoomie.web.services.impl;

import com.yoomie.web.models.CartItem;
import com.yoomie.web.models.Product;
import com.yoomie.web.repositories.ProductRepository;
import com.yoomie.web.services.CartService;
import com.yoomie.web.services.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final CartService cartService;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void checkout(Long cashierId) {
        // 1. Ambil semua item cart untuk kasir ini
        List<CartItem> cartItems = cartService.getCartItemsByCashierId(cashierId);

        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart untuk kasir ini kosong.");
        }

        // 2. Loop setiap item, validasi stok, dan kurangi stok
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            if (product == null || product.getId() == null) {
                throw new IllegalStateException("CartItem tidak berisi product yang valid.");
            }

            // Ambil product yang dikelola oleh JPA (managed entity)
            Product managedProduct = productRepository.findById(product.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Product tidak ditemukan dengan id: " + product.getId()
                    ));

            int currentStock = managedProduct.getStock();
            int quantity = cartItem.getQuantity();

            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity pada cartItem tidak valid: " + quantity);
            }

            if (currentStock <= 0) {
                // Tidak ada stok sama sekali
                throw new IllegalArgumentException(
                        managedProduct.getName() + " sudah habis."
                );
            }

            if (quantity > currentStock) {
                // Contoh pesan: "Produk B hanya tersisa 5"
                throw new IllegalArgumentException(
                        managedProduct.getName() + " hanya tersisa " + currentStock
                );
            }

            // Stok cukup -> kurangi stok
            managedProduct.setStock(currentStock - quantity);
            productRepository.save(managedProduct);
        }

        // OPTIONAL: kalau kamu ingin cart dikosongkan setelah transaksi sukses,
        // bisa tambahkan method clearCart(cashierId) di CartService dan panggil di sini:
        //
        // cartService.clearCart(cashierId);
    }
}
