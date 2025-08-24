package com.example.psysfinsta.ui.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.psysfinsta.data.entity.Budget;
import com.example.psysfinsta.data.repository.BudgetRepository;

import java.util.List;

public class BudgetViewModel extends AndroidViewModel {
    private final BudgetRepository repository;

    public BudgetViewModel(Application application) {
        super(application);
        repository = new BudgetRepository(application);
    }

    public LiveData<List<Budget>> getBudgetsForMonth(long monthTimestamp) {
        return repository.getBudgetsForMonth(monthTimestamp);
    }

    public void insert(Budget budget) {
        repository.insert(budget);
    }

    public void update(Budget budget) {
        repository.update(budget);
    }

    public void delete(Budget budget) {
        repository.delete(budget);
    }
}