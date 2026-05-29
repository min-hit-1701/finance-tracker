package com.uit.minhho.financetracker.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.uit.minhho.financetracker.data.local.entity.Budget;
import java.util.List;

@Dao
public interface BudgetDao {
    @Insert
    void insert(Budget budget);

    @Update
    void update(Budget budget);

    @Delete
    void delete(Budget budget);

    @Query("SELECT * FROM budgets")
    LiveData<List<Budget>> getAllBudgets();

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId LIMIT 1")
    LiveData<Budget> getBudgetByCategory(int categoryId);

    @Query("UPDATE budgets SET spentAmount = spentAmount + :amount WHERE categoryId = :categoryId")
    void updateBudgetSpent(int categoryId, double amount);
}
