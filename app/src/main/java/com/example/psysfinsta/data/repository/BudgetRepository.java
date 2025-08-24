package com.example.psysfinsta.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.psysfinsta.data.dao.BudgetDao;
import com.example.psysfinsta.data.db.AppDatabase;
import com.example.psysfinsta.data.entity.Budget;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BudgetRepository {
    private final BudgetDao budgetDao;
    private final Executor executor;

    public BudgetRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        budgetDao = db.budgetDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Budget>> getBudgetsForMonth(long monthTimestamp) {
        return budgetDao.getBudgetsForMonth(monthTimestamp);
    }

    public void insert(Budget budget) {
        executor.execute(() -> budgetDao.insert(budget));
    }

    public void update(Budget budget) {
        executor.execute(() -> budgetDao.update(budget));
    }

    public void delete(Budget budget) {
        executor.execute(() -> budgetDao.delete(budget));
    }
}