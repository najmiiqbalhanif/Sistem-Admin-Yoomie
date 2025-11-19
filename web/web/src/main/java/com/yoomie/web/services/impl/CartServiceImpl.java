package com.yoomie.web.services.impl;

import com.yoomie.web.models.Cart;
import com.yoomie.web.models.CartItem;
import com.yoomie.web.models.Cashier;
import com.yoomie.web.models.Product;
import com.yoomie.web.repositories.CartItemRepository;
import com.yoomie.web.repositories.CartRepository;
import com.yoomie.web.repositories.ProductRepository;
import com.yoomie.web.repositories.CashierRepository;
import com.yoomie.web.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CashierRepository cashierRepository;

    @Override
    public Cart getCartByCashierId(Long cashierId) {
        return cartRepository.findByCashierId(cashierId);
    }

    @Override
    public List<CartItem> getCartItemsByCashierId(Long cashierId) {
        Cart cart = cartRepository.findByCashierId(cashierId);
        if (cart == null) {
            // Jika Cart tidak ditemukan, kembalikan daftar kosong
            return List.of();
        }
        return cartItemRepository.findByCartId(cart.getId());
    }

    @Override
    @Transactional // Pastikan ini ada karena ada modifikasi database
    public void addToCart(Long cashierId, Long productId) {
        Cart cart = cartRepository.findByCashierId(cashierId);

        if (cart == null) {
            Cashier cashier = cashierRepository.findById(cashierId)
                    .orElseThrow(() -> new IllegalArgumentException("Cashier not found with ID: " + cashierId));
            cart = new Cart();
            cart.setCashier(cashier);
            cart = cartRepository.save(cart);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        Optional<CartItem> existingCartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            cartItem.setSubTotal(cartItem.getQuantity() * product.getPrice());
            cartItemRepository.save(cartItem);
        } else {
            CartItem newCartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(1)
                    .subTotal(product.getPrice())
                    .build();

            cartItemRepository.save(newCartItem);
            cart.getCartItems().add(newCartItem); // Pastikan ini juga update koleksi di entitas Cart
        }

        recalculateCartTotalPrice(cart); // Panggil fungsi pembantu untuk hitung ulang total harga
    }

    // --- IMPLEMENTASI METODE BARU ---

    @Override
    @Transactional
    public void decreaseProductQuantity(Long cashierId, Long productId) {
        Cart cart = cartRepository.findByCashierId(cashierId);
        if (cart == null) {
            throw new IllegalArgumentException("Cart not found for cashier ID: " + cashierId);
        }

        Optional<CartItem> optionalCartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (optionalCartItem.isPresent()) {
            CartItem cartItem = optionalCartItem.get();
            if (cartItem.getQuantity() > 1) {
                cartItem.setQuantity(cartItem.getQuantity() - 1);
                cartItem.setSubTotal(cartItem.getQuantity() * cartItem.getProduct().getPrice());
                cartItemRepository.save(cartItem);
            } else {
                // Jika kuantitas menjadi 1 dan dikurangi, hapus item dari keranjang
                removeProductFromCart(cashierId, productId);
                return; // Keluar dari method setelah penghapusan
            }
            recalculateCartTotalPrice(cart); // Hitung ulang total harga setelah perubahan
        } else {
            throw new IllegalArgumentException("Product with ID " + productId + " not found in cart for cashier " + cashierId);
        }
    }

    @Override
    @Transactional
    public void removeProductFromCart(Long cashierId, Long productId) {
        Cart cart = cartRepository.findByCashierId(cashierId);
        if (cart == null) {
            throw new IllegalArgumentException("Cart not found for cashier ID: " + cashierId);
        }

        Optional<CartItem> optionalCartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (optionalCartItem.isPresent()) {
            CartItem cartItem = optionalCartItem.get();
            cart.getCartItems().remove(cartItem); // Hapus dari koleksi di Cart
            cartItemRepository.delete(cartItem); // Hapus dari database
            // cartRepository.save(cart); // Tidak perlu save cart karena @Transactional akan otomatis sinkron
            recalculateCartTotalPrice(cart); // Hitung ulang total harga setelah penghapusan
        } else {
            throw new IllegalArgumentException("Product with ID " + productId + " not found in cart for cashier " + cashierId);
        }
    }

    @Override
    @Transactional
    public void updateProductQuantity(Long cashierId, Long productId, int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }

        Cart cart = cartRepository.findByCashierId(cashierId);
        if (cart == null) {
            throw new IllegalArgumentException("Cart not found for cashier ID: " + cashierId);
        }

        Optional<CartItem> optionalCartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (optionalCartItem.isPresent()) {
            CartItem cartItem = optionalCartItem.get();
            if (newQuantity == 0) {
                // Jika quantity diupdate menjadi 0, hapus item
                removeProductFromCart(cashierId, productId);
            } else {
                // Perbarui kuantitas dan subTotal
                cartItem.setQuantity(newQuantity);
                cartItem.setSubTotal(cartItem.getProduct().getPrice() * newQuantity);
                cartItemRepository.save(cartItem);
                recalculateCartTotalPrice(cart); // Hitung ulang total harga setelah update
            }
        } else {
            // Jika produk tidak ditemukan di keranjang dan newQuantity > 0, mungkin ingin menambahkannya
            // Namun, untuk kasus update, kita asumsikan produk sudah ada.
            if (newQuantity > 0) {
                throw new IllegalArgumentException("Product with ID " + productId + " not found in cart for cashier " + cashierId + ". Cannot update quantity.");
            }
            // Jika newQuantity == 0 dan produk tidak ada, tidak perlu melakukan apa-apa (sudah seperti dihapus)
        }
    }

    // --- AKHIR IMPLEMENTASI METODE BARU ---

    @Override
    @Transactional
    public void clearCart(Long cashierId) {
        Cart cart = cartRepository.findByCashierId(cashierId);
        if (cart != null) {
            // Hapus semua CartItem yang terkait dengan Cart ini
            cartItemRepository.deleteAll(cart.getCartItems()); // Gunakan deleteAll(Iterable)
            cart.getCartItems().clear(); // Kosongkan koleksi di entitas Cart

            cart.setTotalPrice(0);
            cartRepository.save(cart);
        }
    }

    // Metode pembantu untuk menghitung ulang total harga keranjang
    private void recalculateCartTotalPrice(Cart cart) {
        double totalPrice = cart.getCartItems().stream()
                .mapToDouble(item -> item.getSubTotal())
                .sum();
        cart.setTotalPrice(totalPrice);
        cartRepository.save(cart); // Simpan cart dengan total harga yang diperbarui
    }
}