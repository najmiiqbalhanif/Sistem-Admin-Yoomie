package com.athletezone.web.repositories;

import com.athletezone.web.models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long cartId);
    void deleteAllByCartId(Long cartId);
}
