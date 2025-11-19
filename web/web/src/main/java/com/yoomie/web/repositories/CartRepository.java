package com.yoomie.web.repositories;

import com.yoomie.web.models.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findByCashierId(Long cashierId);
}
