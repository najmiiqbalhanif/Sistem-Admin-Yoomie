package com.yoomie.web.repositories;

import com.yoomie.web.models.Cashier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CashierRepository extends JpaRepository<Cashier, Long> {

    // Menambahkan method untuk mencari cashier berdasarkan email
    Optional<Cashier> findByEmail(String email);
}