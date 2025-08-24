package com.example.psysfinsta;

import android.app.Application;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.psysfinsta.worker.TransactionWorker;

import java.util.concurrent.TimeUnit;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 1️⃣ Run once at app start
        OneTimeWorkRequest startupRequest = new OneTimeWorkRequest.Builder(TransactionWorker.class)
                .setInputData(new Data.Builder()
                        .putString(TransactionWorker.KEY_OPERATION, "check_recurrence")
                        .build())
                .build();

        WorkManager.getInstance(this).enqueue(startupRequest);

        // 2️⃣ Schedule daily background check
        PeriodicWorkRequest dailyRequest =
                new PeriodicWorkRequest.Builder(TransactionWorker.class, 1, TimeUnit.DAYS)
                        .setInputData(new Data.Builder()
                                .putString(TransactionWorker.KEY_OPERATION, "check_recurrence")
                                .build())
                        .build();

        // Use unique name so we don’t schedule duplicates
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "daily_recurrence_check",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyRequest
        );
    }
}