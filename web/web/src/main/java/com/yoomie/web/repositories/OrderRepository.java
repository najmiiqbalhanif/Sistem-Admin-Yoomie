package com.yoomie.web.repositories;

import com.yoomie.web.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByCashierId(Long cashierId);
}
