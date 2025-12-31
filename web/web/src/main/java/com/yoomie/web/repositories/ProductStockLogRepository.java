package com.yoomie.web.repositories;

import com.yoomie.web.models.ProductStockLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductStockLogRepository extends JpaRepository<ProductStockLog, Long> {

    List<ProductStockLog> findByProduct_IdOrderByCreatedOnDesc(Long productId);

    List<ProductStockLog> findAllByOrderByCreatedOnDesc();

    void deleteByProduct_Id(Long productId);
}

