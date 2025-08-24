package com.example.psysfinsta.domain.service;

import androidx.lifecycle.LiveData;

import com.example.psysfinsta.data.entity.TransactionEntity;
import com.example.psysfinsta.data.entity.TransactionType;
import com.example.psysfinsta.data.entity.TransactionWithTags;
import com.example.psysfinsta.data.repository.TransactionRepository;

import java.util.List;

public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void addTransaction(TransactionEntity transactionEntity, List<String> tagNames) {
        if (transactionEntity.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (transactionEntity.getType() == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }

        transactionRepository.insert(transactionEntity, tagNames);
    }

    public void deleteTransaction(TransactionEntity transactionEntity) {
        transactionRepository.delete(transactionEntity);
    }

    public void updateTransaction(TransactionEntity transactionEntity, List<String> tagNames) {
        transactionRepository.update(transactionEntity, tagNames);
    }


    public LiveData<List<TransactionWithTags>> getTransactionsByType(TransactionType type) {
        return transactionRepository.getByType(type);
    }

    // Add more logic methods as needed (e.g., monthly summary, balance, filtering)
}
