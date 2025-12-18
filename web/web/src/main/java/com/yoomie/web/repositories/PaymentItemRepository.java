package com.yoomie.web.repositories;

import com.yoomie.web.models.PaymentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PaymentItemRepository extends JpaRepository<PaymentItem, Long> {
}
