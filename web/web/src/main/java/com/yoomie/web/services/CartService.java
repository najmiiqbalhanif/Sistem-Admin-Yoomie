package com.yoomie.web.services;

import com.yoomie.web.models.Cart;
import com.yoomie.web.models.CartItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public interface CartService {
    public Cart getCartByCashierId(Long cashierId);

    public List<CartItem> getCartItemsByCashierId(Long cashierId);

    public void addToCart(Long cashierId, Long productId);

    // --- TAMBAHAN BARU ---
    public void decreaseProductQuantity(Long cashierId, Long productId);
    public void removeProductFromCart(Long cashierId, Long productId);
    public void updateProductQuantity(Long cashierId, Long productId, int newQuantity);
    // --- AKHIR TAMBAHAN ---

    public void clearCart(Long cashierId);
}