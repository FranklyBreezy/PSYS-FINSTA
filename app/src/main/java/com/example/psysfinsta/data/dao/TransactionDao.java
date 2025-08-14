package com.example.psysfinsta.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.psysfinsta.data.entity.TransactionEntity;
import com.example.psysfinsta.data.entity.Tag;
import com.example.psysfinsta.data.entity.TransactionTagCrossRef;
import com.example.psysfinsta.data.entity.TransactionWithTags;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    long insert(TransactionEntity transaction);

    @Update
    void update(TransactionEntity transaction);

    @Delete
    void delete(TransactionEntity transaction);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTag(Tag tag);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTransactionTagCrossRef(TransactionTagCrossRef crossRef);

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    LiveData<List<TransactionWithTags>> getAllTransactionsWithTags();

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    LiveData<TransactionWithTags> getTransactionWithTagsById(int id);

    @Transaction
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    LiveData<List<TransactionWithTags>> getTransactionsByType(String type);

    @Transaction
    @Query("SELECT * FROM transactions WHERE recurrence != 'NONE' ORDER BY date ASC")
    LiveData<List<TransactionWithTags>> getRecurringTransactions();

    @Transaction
    @Query("SELECT * FROM transactions " +
            "WHERE (:type IS NULL OR type = :type) " +
            "AND date BETWEEN :startDate AND :endDate " +
            "ORDER BY date DESC")
    LiveData<List<TransactionWithTags>> getFilteredTransactions(String type, long startDate, long endDate);

    @Transaction
    @Query("SELECT DISTINCT t.* FROM transactions t " +
            "INNER JOIN transaction_tag_cross_ref tx_tag ON t.id = tx_tag.transactionId " +
            "INNER JOIN tags tag ON tx_tag.tagId = tag.tagId " +
            "WHERE (:type IS NULL OR t.type = :type) " +
            "AND t.date BETWEEN :startDate AND :endDate " +
            "AND tag.tag_name IN (:tagNames) " +
            "ORDER BY t.date DESC")
    LiveData<List<TransactionWithTags>> getFilteredTransactionsByTags(
            String type,
            long startDate,
            long endDate,
            List<String> tagNames
    );
}
