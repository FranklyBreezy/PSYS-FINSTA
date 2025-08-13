package com.example.psysfinsta.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.psysfinsta.data.entity.Income;

import java.util.List;

@Dao
public interface IncomeDao {
    @Insert
    void insertIncome(Income income);

    @Delete
    void deleteIncome(Income income);

    @Update
    void updateIncome(Income income);

    @Query("SELECT * FROM incomes ORDER BY date DESC")
    LiveData<List<Income>> getAllIncomes();

    @Query("SELECT * FROM incomes ORDER BY date DESC LIMIT :limit OFFSET :offset")
    List<Income> getIncomesPaged(int limit, int offset);
}
