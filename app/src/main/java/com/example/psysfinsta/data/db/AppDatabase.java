package com.example.psysfinsta.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.psysfinsta.data.dao.ExpenseDao;
import com.example.psysfinsta.data.dao.IncomeDao;
import com.example.psysfinsta.data.entity.Expense;
import com.example.psysfinsta.data.entity.Income;

@Database(
        entities = {Expense.class , Income.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ExpenseDao expenseDao();
    public abstract IncomeDao incomeDao();
    private static volatile  AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context){
        if(INSTANCE == null){
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "psysfinsta_database").build();
                }
            }
        }
        return INSTANCE;
    }
}
