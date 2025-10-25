package com.athletezone.web.services;

import com.athletezone.web.models.Payment;

public interface PaymentService {
    public Payment getPaymentByOrderId(Long orderId);
    public Payment savePayment(Payment payment);
}
