package com.example.psysfinsta.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.psysfinsta.data.dao.ExpenseDao;
import com.example.psysfinsta.data.db.AppDatabase;
import com.example.psysfinsta.data.entity.Expense;

import java.util.List;

public class ExpenseRepository {
    private final ExpenseDao expenseDao;
    private final LiveData<List<Expense>> allExpenses;

    public ExpenseRepository(Application application){
        AppDatabase db = AppDatabase.getInstance(application);
        expenseDao = db.expenseDao();
        allExpenses = expenseDao.getAllExpenses();
    }

    public void insert(Expense expense){new Thread(()-> expenseDao.insertExpense(expense)).start();}
    public void delete(Expense expense){new Thread(()-> expenseDao.deleteExpense(expense)).start();}
    public void update(Expense expense){new Thread(()-> expenseDao.updateExpense(expense)).start();}

    public LiveData<List<Expense>> getAllExpenses(){return allExpenses;}
    public List<Expense> getExpensesPaged(int limit, int offset){return expenseDao.getExpensesPaged(limit,offset);}

}
