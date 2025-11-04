package com.yoomie.web.services.impl;

import com.yoomie.web.models.Transaction;
import com.yoomie.web.models.Payment;
import com.yoomie.web.repositories.OrderRepository;
import com.yoomie.web.repositories.PaymentRepository;
import com.yoomie.web.services.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Override
    public Payment getPaymentByTransactionId(Long transactionId) {
        // Cari Payment berdasarkan transactionId
        Transaction transaction = orderRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
        return transaction.getPayment();
    }

    @Override
    // Menyimpan payment baru
    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }
}
