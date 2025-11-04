package com.yoomie.web.services;

import com.yoomie.web.models.Payment;

public interface PaymentService {
    public Payment getPaymentByTransactionId(Long transactionId);
    public Payment savePayment(Payment payment);
}
