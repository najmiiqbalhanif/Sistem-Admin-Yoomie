package com.yoomie.web.controllerREST;

import com.yoomie.web.dto.TransactionDTO;
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
@RequestMapping("/api")
public class paymentControllerREST {

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

        if (request.getCashierId() == null) {
            return ResponseEntity.badRequest().body("cashierId is required");
        }

        if (request.getTotalAmount() == null) {
            return ResponseEntity.badRequest().body("totalAmount is required");
        }

        if (request.getPaymentItems() == null || request.getPaymentItems().isEmpty()) {
            return ResponseEntity.badRequest().body("paymentItems is required");
        }

        Cashier cashier = cashierService.getCashierById(request.getCashierId());
        if (cashier == null) {
            return ResponseEntity.badRequest().body("Cashier not found");
        }

        String method = request.getPaymentMethod() != null ? request.getPaymentMethod().toLowerCase() : "";
        if (!(method.equals("cash") || method.equals("mandiri") || method.equals("bca"))) {
            return ResponseEntity.badRequest().body("Invalid payment method. Use: cash / mandiri / bca");
        }

        double total = request.getTotalAmount();

        Payment payment = new Payment();
        payment.setCashier(cashier);
        payment.setPaymentMethod(method);
        payment.setTotalAmount(total);

        if (method.equals("cash")) {
            if (request.getCashPaid() == null) {
                return ResponseEntity.badRequest().body("cashPaid is required for cash payment");
            }

            double cashPaid = request.getCashPaid();

            if (cashPaid < total) {
                return ResponseEntity.badRequest().body("Uang customer kurang");
            }

            payment.setCashPaid(cashPaid);
            payment.setChangeAmount(cashPaid - total);
        } else {
            payment.setCashPaid(null);
            payment.setChangeAmount(null);
        }

        try {
            Transaction transaction = transactionService.processCheckout(
                    cashier,
                    payment,
                    request.getPaymentItems()
            );
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to process checkout: " + e.getMessage());
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        List<TransactionDTO> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/cashier/{cashierId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByCashierId(@PathVariable Long cashierId) {

        List<Transaction> transactions = transactionService.getTransactionsByCashierId(cashierId);

        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(transaction -> TransactionDTO.builder()
                        .id(transaction.getId())
                        .cashierId(transaction.getCashier().getId())
                        .cashierName(transaction.getCashier().getCashierName())
                        .createdOn(transaction.getPayment().getCreatedOn().toString())
                        .cartSummary(transaction.getPayment().getPaymentItems().stream()
                                .map(item -> item.getProductName() + " x " + item.getQuantity())
                                .collect(Collectors.joining(", ")))
                        .totalAmount(transaction.getPayment().getTotalAmount())
                        .paymentMethod(transaction.getPayment().getPaymentMethod())
                        .cashPaid(transaction.getPayment().getCashPaid())
                        .changeAmount(transaction.getPayment().getChangeAmount())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(transactionDTOs);
    }
}
