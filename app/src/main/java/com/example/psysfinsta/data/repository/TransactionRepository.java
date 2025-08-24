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

import java.util.Calendar;
import java.util.Collections;
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

            if (tagNames != null) {
                for (String tagName : tagNames) {
                    String cleanName = tagName.trim().toLowerCase();

                    Tag tag = tagDao.getTagByName(cleanName);
                    long tagId;

                    if (tag == null) {
                        tagId = tagDao.insertTag(new Tag(cleanName));
                    } else {
                        tagId = tag.getTagId();
                    }

                    transactionDao.insertTransactionTagCrossRef(
                            new TransactionTagCrossRef((int) transactionId, (int) tagId));
                }
            }
        });
    }


    public void update(TransactionEntity transactionEntity, List<String> tagNames) {
        executor.execute(() -> {
            transactionDao.update(transactionEntity);
            int txId = transactionEntity.getId();

            transactionDao.deleteTransactionTagCrossRefs(txId);

            if (tagNames != null) {
                for (String tagName : tagNames) {
                    String cleanName = tagName.trim().toLowerCase();

                    Tag tag = tagDao.getTagByName(cleanName);
                    long tagId;

                    if (tag == null) {
                        tagId = tagDao.insertTag(new Tag(cleanName));
                    } else {
                        tagId = tag.getTagId();
                    }

                    transactionDao.insertTransactionTagCrossRef(
                            new TransactionTagCrossRef(txId, (int) tagId));
                }
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

    public TransactionEntity getTransactionByIdSync(int id) {
        return transactionDao.getTransactionByIdSync(id);
    }

    public LiveData<TransactionWithTags> getById(int id) {
        return transactionDao.getTransactionWithTagsById(id);
    }

    public void delete(TransactionEntity transactionEntity) {
        executor.execute(() -> transactionDao.delete(transactionEntity));
    }

    public void deleteFuture(long fromDate) {
        executor.execute(() -> transactionDao.deleteFutureTransactions(fromDate));
    }

    public LiveData<List<TransactionWithTags>> getFilteredTransactions(
            TransactionType type, long startDate, long endDate) {
        String typeStr = (type != null) ? type.name() : null;
        return transactionDao.getFilteredTransactions(typeStr, startDate, endDate);
    }

    public List<TransactionEntity> getRecurringTransactionsSync() {
        return transactionDao.getRecurringTransactionsSync();
    }

    public boolean checkIfOccurrenceExists(TransactionEntity recurringTx) {
        long periodStart = calculateCurrentPeriodStart(recurringTx);
        long periodEnd = calculateCurrentPeriodEnd(recurringTx);
        int count = transactionDao.countOccurrencesInPeriod(
                recurringTx.getRecurringGroupId(), periodStart, periodEnd);
        return count > 0;
    }

    public TransactionEntity createNextOccurrence(TransactionEntity recurringTx) {
        TransactionEntity newTx = new TransactionEntity();
        newTx.setAmount(recurringTx.getAmount());
        newTx.setType(recurringTx.getType());
        newTx.setDescription(recurringTx.getDescription());
        newTx.setRecurringGroupId(recurringTx.getRecurringGroupId());
        newTx.setDate(calculateNextDate(recurringTx));
        newTx.setFrequency(recurringTx.getFrequency());
        newTx.setIsRecurring(false);  // Concrete instance
        return newTx;
    }

    public void insertSync(TransactionEntity tx) {
        transactionDao.insert(tx);
    }

    private long calculateCurrentPeriodStart(TransactionEntity tx) {
        Calendar calendar = Calendar.getInstance();
        String freq = tx.getFrequency();
        switch (freq.toLowerCase()) {
            case "daily":
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                return calendar.getTimeInMillis();
            case "weekly":
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                return calendar.getTimeInMillis();
            case "monthly":
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                return calendar.getTimeInMillis();
            default:
                return tx.getDate();
        }
    }

    private long calculateCurrentPeriodEnd(TransactionEntity tx) {
        Calendar calendar = Calendar.getInstance();
        String freq = tx.getFrequency();
        switch (freq.toLowerCase()) {
            case "daily":
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                return calendar.getTimeInMillis();
            case "weekly":
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                calendar.add(Calendar.DAY_OF_WEEK, 6);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                return calendar.getTimeInMillis();
            case "monthly":
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                return calendar.getTimeInMillis();
            default:
                return tx.getDate();
        }
    }

    private long calculateNextDate(TransactionEntity tx) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(tx.getDate());
        String freq = tx.getFrequency();
        switch (freq.toLowerCase()) {
            case "daily":
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                break;
            case "weekly":
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case "monthly":
                calendar.add(Calendar.MONTH, 1);
                break;
            default:
                // default to same date if frequency is none
                break;
        }
        return calendar.getTimeInMillis();
    }
}
