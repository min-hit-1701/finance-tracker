package com.uit.minhho.financetracker.data.local.database;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

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

import java.util.concurrent.Executors;

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
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    Executors.newSingleThreadExecutor().execute(() -> {
                                        seedDefaultData(INSTANCE);
                                    });
                                }
                            })
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void seedDefaultData(AppDatabase db) {
        CategoryDao catDao = db.categoryDao();
        catDao.insert(new Category("Ăn uống", 0, "#F44336", false));
        catDao.insert(new Category("Di chuyển", 0, "#FF9800", false));
        catDao.insert(new Category("Mua sắm", 0, "#E91E63", false));
        catDao.insert(new Category("Giải trí", 0, "#9C27B0", false));
        catDao.insert(new Category("Tiền lương", 0, "#4CAF50", true));
        catDao.insert(new Category("Tiền thưởng", 0, "#8BC34A", true));

        WalletDao walletDao = db.walletDao();
        walletDao.insert(new Wallet("Ví tiền mặt", 0.0, "Cash", false));
        walletDao.insert(new Wallet("Tài khoản chính", 0.0, "Bank", false));
        walletDao.insert(new Wallet("Quỹ kinh doanh", 0.0, "Business", true));
    }
}
