package com.yoomie.web.controllerREST;

import com.yoomie.web.dto.TransactionDTO; // Import TransactionDTO
import com.yoomie.web.dto.PaymentDTO;
import com.yoomie.web.dto.PaymentItemDTO;
import com.yoomie.web.models.Transaction;
import com.yoomie.web.models.Payment;
import com.yoomie.web.models.Cashier;
import com.yoomie.web.services.TransactionService;
import com.yoomie.web.services.CashierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
// Ubah base request mapping atau buat controller baru untuk transactions
@RequestMapping("/api")
public class paymentControllerREST { // Atau rename menjadi TransactionControllerREST jika lebih sesuai
    private final TransactionService transactionService;
    private final CashierService cashierService;

    public paymentControllerREST(TransactionService transactionService, CashierService cashierService) {
        this.transactionService = transactionService;
        this.cashierService = cashierService;
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
        Cashier cashier = cashierService.getCashierById(request.getCashierId());
        if (cashier == null) {
            return ResponseEntity.badRequest().body("Cashier not found");
        }

        Payment payment = new Payment();
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTotalAmount(request.getTotalAmount());

        try {
            Transaction transaction = transactionService.processCheckout(cashier, payment, request.getPaymentItems());
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

    // --- NEW: Endpoint untuk mendapatkan transaction berdasarkan cashier ID (lebih relevan untuk aplikasi cashier) ---
    @GetMapping("/transactions/cashier/{cashierId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByCashierId(@PathVariable Long cashierId) {
        List<Transaction> transactions = transactionService.getTransactionsByCashierId(cashierId);
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(transaction -> {
                    return TransactionDTO.builder()
                            .id(transaction.getId())
                            .cashierId(transaction.getCashier().getId())
                            .cashierName(transaction.getCashier().getCashierName())
                            .createdOn(transaction.getPayment().getCreatedOn().toString())
                            .cartSummary(transaction.getPayment().getPaymentItems().stream()
                                    .map(item -> item.getProductName() + " x " + item.getQuantity())
                                    .collect(Collectors.joining(", ")))
                            .totalAmount(transaction.getPayment().getTotalAmount())
                            .paymentMethod(transaction.getPayment().getPaymentMethod())
                            .build();
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactionDTOs);
    }
}