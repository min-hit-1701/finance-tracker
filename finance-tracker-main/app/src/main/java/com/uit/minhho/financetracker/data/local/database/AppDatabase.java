package com.uit.minhho.financetracker.data.local.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.uit.minhho.financetracker.data.local.dao.BudgetDao;
import com.uit.minhho.financetracker.data.local.dao.CategoryDao;
import com.uit.minhho.financetracker.data.local.dao.TransactionDao;
import com.uit.minhho.financetracker.data.local.dao.UserDao;
import com.uit.minhho.financetracker.data.local.dao.WalletDao;
import com.uit.minhho.financetracker.data.local.entity.Budget;
import com.uit.minhho.financetracker.data.local.entity.Category;
import com.uit.minhho.financetracker.data.local.entity.Transaction;
import com.uit.minhho.financetracker.data.local.entity.User;
import com.uit.minhho.financetracker.data.local.entity.Wallet;

@Database(entities = {User.class, Wallet.class, Category.class, Transaction.class, Budget.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract WalletDao walletDao();
    public abstract CategoryDao categoryDao();
    public abstract TransactionDao transactionDao();
    public abstract BudgetDao budgetDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "finance_tracker_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
