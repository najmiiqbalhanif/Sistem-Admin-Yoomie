package com.yoomie.web.controllerREST;

import com.yoomie.web.dto.TransactionDTO; // Import TransactionDTO
import com.yoomie.web.dto.PaymentDTO;
import com.yoomie.web.dto.PaymentItemDTO;
import com.yoomie.web.models.Transaction;
import com.yoomie.web.models.Payment;
import com.yoomie.web.models.User;
import com.yoomie.web.services.TransactionService;
import com.yoomie.web.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
// Ubah base request mapping atau buat controller baru untuk transactions
@RequestMapping("/api")
public class checkoutPaymentControllerREST { // Atau rename menjadi TransactionControllerREST jika lebih sesuai
    private final TransactionService transactionService;
    private final UserService userService;

    public checkoutPaymentControllerREST(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    public static class CheckoutPaymentRequest extends PaymentDTO {
        private List<PaymentItemDTO> paymentItems;

        public List<PaymentItemDTO> getPaymentItems() {
            return paymentItems;
        }

        public void setPaymentItems(List<PaymentItemDTO> paymentItems) {
            this.paymentItems = paymentItems;
        }
    }

    @PostMapping("/checkoutpayment/submit")
    public ResponseEntity<?> submitCheckout(@RequestBody CheckoutPaymentRequest request) {
        User user = userService.getUserById(request.getUserId());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        Payment payment = new Payment();
        payment.setAddress(request.getAddress());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTotalAmount(request.getTotalAmount());
        payment.setStatus("Paid"); // Status awal mungkin PENDING, Paid, etc. Sesuaikan dengan logika Anda.

        try {
            Transaction transaction = transactionService.processCheckout(user, payment, request.getPaymentItems());
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to process checkout: " + e.getMessage());
        }
    }

    // --- NEW: Endpoint untuk mendapatkan semua transaction ---
    @GetMapping("/transactions") // Endpoint yang akan kita panggil dari Flutter
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        List<TransactionDTO> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    // --- NEW: Endpoint untuk mendapatkan transaction berdasarkan user ID (lebih relevan untuk aplikasi user) ---
    @GetMapping("/transactions/user/{userId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByUserId(@PathVariable Long userId) {
        List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(transaction -> {
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
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactionDTOs);
    }
}