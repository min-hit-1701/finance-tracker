package com.uit.minhho.financetracker.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.uit.minhho.financetracker.data.local.entity.Transaction;
import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM transactions WHERE isBusiness = :isBusiness ORDER BY timestamp DESC")
    LiveData<List<Transaction>> getTransactions(boolean isBusiness);

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY timestamp DESC")
    LiveData<List<Transaction>> getTransactionsByCategory(int categoryId);

    @Query("SELECT SUM(amount) FROM transactions WHERE isIncome = 1 AND isBusiness = :isBusiness")
    LiveData<Double> getTotalIncome(boolean isBusiness);

    @Query("SELECT SUM(amount) FROM transactions WHERE isIncome = 0 AND isBusiness = :isBusiness")
    LiveData<Double> getTotalExpense(boolean isBusiness);

    @Query("SELECT * FROM transactions WHERE isBusiness = :isBusiness ORDER BY timestamp DESC LIMIT :limit")
    LiveData<List<Transaction>> getRecentTransactions(boolean isBusiness, int limit);

    @Query("SELECT * FROM transactions WHERE isBusiness = :isBusiness AND timestamp BETWEEN :fromTimestamp AND :toTimestamp ORDER BY timestamp DESC")
    LiveData<List<Transaction>> getTransactionsByPeriod(boolean isBusiness, long fromTimestamp, long toTimestamp);

    @Query("SELECT SUM(amount) FROM transactions WHERE isIncome = 1 AND isBusiness = :isBusiness AND timestamp BETWEEN :fromTimestamp AND :toTimestamp")
    LiveData<Double> getTotalIncomeByPeriod(boolean isBusiness, long fromTimestamp, long toTimestamp);

    @Query("SELECT SUM(amount) FROM transactions WHERE isIncome = 0 AND isBusiness = :isBusiness AND timestamp BETWEEN :fromTimestamp AND :toTimestamp")
    LiveData<Double> getTotalExpenseByPeriod(boolean isBusiness, long fromTimestamp, long toTimestamp);
}
