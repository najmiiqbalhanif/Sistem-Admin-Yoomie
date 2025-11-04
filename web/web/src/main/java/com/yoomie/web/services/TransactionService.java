package com.yoomie.web.services;

import com.yoomie.web.dto.TransactionDTO;
import com.yoomie.web.dto.PaymentItemDTO;
import com.yoomie.web.models.Transaction;
import com.yoomie.web.models.Payment;
import com.yoomie.web.models.User;

import java.util.List;


public interface TransactionService {
    public List<TransactionDTO> getAllTransactions();

    public List<Transaction> getTransactionsByUserId(Long userId);

    public void updateTransactionStatus(Long transactionId, String newStatus);

    public Transaction saveTransaction(Transaction transaction);

    public Transaction processCheckout(User user, Payment payment, List<PaymentItemDTO> paymentItems);

}
