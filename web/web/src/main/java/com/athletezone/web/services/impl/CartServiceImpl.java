package com.athletezone.web.services.impl;

import com.athletezone.web.models.Cart;
import com.athletezone.web.models.CartItem;
import com.athletezone.web.models.Product;
import com.athletezone.web.models.User;
import com.athletezone.web.repositories.CartItemRepository;
import com.athletezone.web.repositories.CartRepository;
import com.athletezone.web.repositories.ProductRepository;
import com.athletezone.web.repositories.UserRepository;
import com.athletezone.web.services.CartService;
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
    private final UserRepository userRepository;

    @Override
    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    @Override
    public List<CartItem> getCartItemsByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            // Jika Cart tidak ditemukan, kembalikan daftar kosong
            return List.of();
        }
        return cartItemRepository.findByCartId(cart.getId());
    }

    @Override
    @Transactional // Pastikan ini ada karena ada modifikasi database
    public void addToCart(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId);

        if (cart == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
            cart = new Cart();
            cart.setUser(user);
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
    public void decreaseProductQuantity(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            throw new IllegalArgumentException("Cart not found for user ID: " + userId);
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
                removeProductFromCart(userId, productId);
                return; // Keluar dari method setelah penghapusan
            }
            recalculateCartTotalPrice(cart); // Hitung ulang total harga setelah perubahan
        } else {
            throw new IllegalArgumentException("Product with ID " + productId + " not found in cart for user " + userId);
        }
    }

    @Override
    @Transactional
    public void removeProductFromCart(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            throw new IllegalArgumentException("Cart not found for user ID: " + userId);
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
            throw new IllegalArgumentException("Product with ID " + productId + " not found in cart for user " + userId);
        }
    }

    @Override
    @Transactional
    public void updateProductQuantity(Long userId, Long productId, int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }

        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            throw new IllegalArgumentException("Cart not found for user ID: " + userId);
        }

        Optional<CartItem> optionalCartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (optionalCartItem.isPresent()) {
            CartItem cartItem = optionalCartItem.get();
            if (newQuantity == 0) {
                // Jika quantity diupdate menjadi 0, hapus item
                removeProductFromCart(userId, productId);
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
                throw new IllegalArgumentException("Product with ID " + productId + " not found in cart for user " + userId + ". Cannot update quantity.");
            }
            // Jika newQuantity == 0 dan produk tidak ada, tidak perlu melakukan apa-apa (sudah seperti dihapus)
        }
    }

    // --- AKHIR IMPLEMENTASI METODE BARU ---

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
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