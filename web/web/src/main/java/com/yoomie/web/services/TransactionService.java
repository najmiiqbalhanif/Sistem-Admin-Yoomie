package com.yoomie.web.services;

import com.yoomie.web.dto.TransactionDTO;
import com.yoomie.web.dto.PaymentItemDTO;
import com.yoomie.web.models.Cashier;
import com.yoomie.web.models.Transaction;
import com.yoomie.web.models.Payment;

import java.util.List;


public interface TransactionService {
    public List<TransactionDTO> getAllTransactions();

    public List<Transaction> getTransactionsByCashierId(Long cashierId);

    public Transaction saveTransaction(Transaction transaction);

    public Transaction processCheckout(Cashier cashier, Payment payment, List<PaymentItemDTO> paymentItems);

}
