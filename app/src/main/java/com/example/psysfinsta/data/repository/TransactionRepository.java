package com.example.psysfinsta.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.psysfinsta.data.dao.TagDao;
import com.example.psysfinsta.data.dao.TransactionDao;
import com.example.psysfinsta.data.db.AppDatabase;
import com.example.psysfinsta.data.entity.Tag;
import com.example.psysfinsta.data.entity.TransactionEntity;
import com.example.psysfinsta.data.entity.TransactionTagCrossRef;
import com.example.psysfinsta.data.entity.TransactionType;
import com.example.psysfinsta.data.entity.TransactionWithTags;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TransactionRepository {

    private final TransactionDao transactionDao;
    private final TagDao tagDao;
    private final Executor executor;

    public TransactionRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        transactionDao = db.transactionDao();
        tagDao = db.tagDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public void insert(TransactionEntity transactionEntity, List<String> tagNames) {
        executor.execute(() -> {
            long transactionId = transactionDao.insert(transactionEntity);

            for (String tagName : tagNames) {
                String cleanName = tagName.trim().toLowerCase();

                Tag tag = tagDao.getTagByName(cleanName);
                int tagId;

                if (tag == null) {
                    tagId = (int) tagDao.insertTag(new Tag(cleanName));
                } else {
                    tagId = tag.getTagId();
                }

                transactionDao.insertTransactionTagCrossRef(new TransactionTagCrossRef((int) transactionId, tagId));
            }
        });
    }

    public LiveData<List<TransactionWithTags>> getAllTransactions() {
        return transactionDao.getAllTransactionsWithTags();
    }

    public LiveData<List<TransactionWithTags>> getByType(TransactionType type) {
        return transactionDao.getTransactionsByType(type.name());
    }

    public LiveData<List<TransactionWithTags>> getRecurring() {
        return transactionDao.getRecurringTransactions();
    }

    public LiveData<TransactionWithTags> getById(int id) {
        return transactionDao.getTransactionWithTagsById(id);
    }

    public void update(TransactionEntity transactionEntity) {
        executor.execute(() -> transactionDao.update(transactionEntity));
    }

    public void delete(TransactionEntity transactionEntity) {
        executor.execute(() -> transactionDao.delete(transactionEntity));
    }

    // ✅ NEW METHOD: Get filtered transactions by type and date range
    public LiveData<List<TransactionWithTags>> getFilteredTransactions(TransactionType type, long startDate, long endDate) {
        String typeStr = (type != null) ? type.name() : null;
        return transactionDao.getFilteredTransactions(typeStr, startDate, endDate);
    }
}
