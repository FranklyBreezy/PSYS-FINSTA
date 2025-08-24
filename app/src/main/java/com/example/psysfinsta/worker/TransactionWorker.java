package com.example.psysfinsta.worker;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.psysfinsta.data.entity.TransactionEntity;
import com.example.psysfinsta.data.repository.TransactionRepository;

import java.util.List;

public class TransactionWorker extends Worker {

    public static final String KEY_OPERATION = "operation";
    public static final String KEY_TRANSACTION_ID = "transactionId";
    private static final String TAG = "TransactionWorker";

    public TransactionWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String operation = getInputData().getString(KEY_OPERATION);
        TransactionRepository repository =
                new TransactionRepository((Application) getApplicationContext());

        try {
            if ("delete".equals(operation)) {
                handleDelete(repository);
            } else if ("check_recurrence".equals(operation)) {
                Log.d(TAG, "Running recurrence check...");
                checkAndInsertRecurringTransactions(repository);
            } else {
                Log.w(TAG, "Unknown operation: " + operation);
            }
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Worker failed", e);
            return Result.failure();
        }
    }

    private void handleDelete(TransactionRepository repository) {
        long txId = getInputData().getLong(KEY_TRANSACTION_ID, -1);
        if (txId != -1) {
            TransactionEntity tx = repository.getTransactionByIdSync((int) txId);
            if (tx != null) {
                repository.delete(tx);
                Log.d(TAG, "Deleted transaction ID: " + txId);
            } else {
                Log.w(TAG, "Transaction not found for ID: " + txId);
            }
        } else {
            Log.w(TAG, "Invalid transaction ID for delete");
        }
    }

    private void checkAndInsertRecurringTransactions(TransactionRepository repository) {
        List<TransactionEntity> recurringTxs = repository.getRecurringTransactionsSync();

        for (TransactionEntity recurringTx : recurringTxs) {
            boolean insertedAny = false;

            // Keep generating until the next occurrence would be in the future
            while (!repository.checkIfOccurrenceExists(recurringTx)) {
                TransactionEntity nextTx = repository.createNextOccurrence(recurringTx);

                if (nextTx.getDate() > System.currentTimeMillis()) {
                    break; // stop if next due date is in the future
                }

                repository.insertSync(nextTx);
                Log.d(TAG, "Inserted recurring transaction for date: " + nextTx.getDate());

                recurringTx = nextTx; // advance to next period
                insertedAny = true;
            }

            if (!insertedAny) {
                Log.d(TAG, "No new occurrences needed for groupId: "
                        + recurringTx.getRecurringGroupId());
            }
        }
    }
}