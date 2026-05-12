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
    long insert(Budget budget);

    @Update
    void update(Budget budget);

    @Delete
    void delete(Budget budget);

    @Query("SELECT * FROM budgets ORDER BY id DESC")
    LiveData<List<Budget>> getAllBudgets();

    @Query("SELECT * FROM budgets WHERE isBusiness = :isBusiness ORDER BY id DESC")
    LiveData<List<Budget>> getBudgets(boolean isBusiness);

    @Query("SELECT * FROM budgets WHERE period = :period ORDER BY id DESC")
    LiveData<List<Budget>> getBudgetsByPeriod(String period);

    @Query("SELECT * FROM budgets WHERE isBusiness = :isBusiness AND period = :period ORDER BY id DESC")
    LiveData<List<Budget>> getBudgetsByPeriod(boolean isBusiness, String period);

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId LIMIT 1")
    LiveData<Budget> getBudgetByCategory(int categoryId);

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND period = :period LIMIT 1")
    Budget getBudgetByCategoryAndPeriodSync(int categoryId, String period);

    @Query("UPDATE budgets SET spentAmount = :spentAmount WHERE id = :budgetId")
    void updateSpentAmount(int budgetId, double spentAmount);
}
