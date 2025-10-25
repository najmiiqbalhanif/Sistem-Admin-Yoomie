package com.athletezone.web.services;

import com.athletezone.web.models.Cart;
import com.athletezone.web.models.CartItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public interface CartService {
    public Cart getCartByUserId(Long userId);

    public List<CartItem> getCartItemsByUserId(Long userId);

    public void addToCart(Long userId, Long productId);

    // --- TAMBAHAN BARU ---
    public void decreaseProductQuantity(Long userId, Long productId);
    public void removeProductFromCart(Long userId, Long productId);
    public void updateProductQuantity(Long userId, Long productId, int newQuantity);
    // --- AKHIR TAMBAHAN ---

    public void clearCart(Long userId);
}