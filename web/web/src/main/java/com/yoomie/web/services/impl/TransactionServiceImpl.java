package com.yoomie.web.services.impl;

// ... (imports lainnya)

import com.yoomie.web.dto.TransactionDTO; // Penting: pastikan TransactionDTO diimport
import com.yoomie.web.dto.PaymentItemDTO;
import com.yoomie.web.models.Transaction;
import com.yoomie.web.models.Payment;
import com.yoomie.web.models.PaymentItem;
import com.yoomie.web.models.User;
import com.yoomie.web.repositories.OrderRepository;
import com.yoomie.web.repositories.PaymentItemRepository;
import com.yoomie.web.repositories.PaymentRepository;
import com.yoomie.web.repositories.ProductRepository;
import com.yoomie.web.services.TransactionService;
import com.yoomie.web.services.CartService; // Ini untuk injeksi CartService
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final OrderRepository orderRepository;
    private final PaymentItemRepository paymentItemRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository; // Pastikan ini ada jika diperlukan
    private final CartService cartService; // Pastikan ini ada

    @Override
    public List<TransactionDTO> getAllTransactions() { // <--- PASTIKAN METHOD INI ADA DAN TIDAK DIKOMENTARI
        return orderRepository.findAll().stream()
                .map(this::transactionToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> getTransactionsByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void updateTransactionStatus(Long transactionId, String newStatus) {
        Transaction transaction = orderRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        Payment payment = transaction.getPayment();
        if (payment != null) {
            payment.setStatus(newStatus);
        }
    }

    @Override
    public Transaction saveTransaction(Transaction transaction) {
        return orderRepository.save(transaction);
    }

    private TransactionDTO transactionToDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .userId(transaction.getUser().getId())
                .username(transaction.getUser().getUsername())
                .createdOn(transaction.getPayment().getCreatedOn().toString())
                .cartSummary(transaction.getPayment().getPaymentItems().stream()
                        .map(item -> item.getProductName() + " x " + item.getQuantity())
                        .collect(Collectors.joining(", ")))
                .totalAmount(transaction.getPayment().getTotalAmount())
                .paymentMethod(transaction.getPayment().getPaymentMethod())
                .paymentStatus(transaction.getPayment().getStatus())
                .address(transaction.getPayment().getAddress())
                .build();
    }

    @Transactional
    public Transaction processCheckout(User user, Payment payment, List<PaymentItemDTO> paymentItems) {
        payment.setStatus("PENDING");
        payment = paymentRepository.save(payment);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setPayment(payment);
        transaction = orderRepository.save(transaction);

        for (PaymentItemDTO itemDTO : paymentItems) {
            PaymentItem paymentItem = new PaymentItem();
            paymentItem.setPayment(payment);
            paymentItem.setProductName(itemDTO.getProductName());
            paymentItem.setQuantity(itemDTO.getQuantity());
            paymentItem.setPrice(itemDTO.getPrice());
            paymentItem.setSubTotal(itemDTO.getSubTotal());

            paymentItemRepository.save(paymentItem);
        }

        cartService.clearCart(user.getId()); // Panggil clearCart di sini

        return transaction;
    }
}