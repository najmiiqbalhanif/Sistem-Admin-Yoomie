package com.yoomie.web.repositories;

import com.yoomie.web.models.Cashier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CashierRepository extends JpaRepository<Cashier, Long> {

    Optional<Cashier> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByCashierName(String cashierName);

    List<Cashier> findAllByDeletedFalse();

    Optional<Cashier> findByEmailAndDeletedFalseAndActiveTrue(String email);

    List<Cashier> findAllByActiveTrue();

    Optional<Cashier> findByEmailAndActiveTrue(String email);

    List<Cashier> findAllByActiveAndDeletedFalse(Boolean active);
}
