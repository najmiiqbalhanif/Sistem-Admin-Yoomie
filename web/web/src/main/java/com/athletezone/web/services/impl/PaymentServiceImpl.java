package com.athletezone.web.services.impl;

import com.athletezone.web.models.Order;
import com.athletezone.web.models.Payment;
import com.athletezone.web.models.Product;
import com.athletezone.web.repositories.OrderRepository;
import com.athletezone.web.repositories.PaymentRepository;
import com.athletezone.web.services.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Override
    public Payment getPaymentByOrderId(Long orderId) {
        // Cari Payment berdasarkan orderId
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        return order.getPayment();
    }

    @Override
    // Menyimpan payment baru
    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }
}
