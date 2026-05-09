package com.uit.minhho.financetracker.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.uit.minhho.financetracker.data.local.dao.BudgetDao;
import com.uit.minhho.financetracker.data.local.dao.CategoryDao;
import com.uit.minhho.financetracker.data.local.dao.TransactionDao;
import com.uit.minhho.financetracker.data.local.dao.UserDao;
import com.uit.minhho.financetracker.data.local.dao.WalletDao;
import com.uit.minhho.financetracker.data.local.database.AppDatabase;
import com.uit.minhho.financetracker.data.local.entity.Budget;
import com.uit.minhho.financetracker.data.local.entity.Category;
import com.uit.minhho.financetracker.data.local.entity.Transaction;
import com.uit.minhho.financetracker.data.local.entity.User;
import com.uit.minhho.financetracker.data.local.entity.Wallet;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppRepository {
    private final UserDao userDao;
    private final WalletDao walletDao;
    private final CategoryDao categoryDao;
    private final TransactionDao transactionDao;
    private final BudgetDao budgetDao;
    private final ExecutorService executorService;

    public AppRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        walletDao = db.walletDao();
        categoryDao = db.categoryDao();
        transactionDao = db.transactionDao();
        budgetDao = db.budgetDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    // Auth methods
    public void registerUser(User user) {
        executorService.execute(() -> userDao.registerUser(user));
    }

    public User login(String email, String password) {
        return userDao.login(email, password);
    }

    // Wallet methods
    public LiveData<List<Wallet>> getWallets(boolean isBusiness) {
        return walletDao.getWallets(isBusiness);
    }

    public void insertWallet(Wallet wallet) {
        executorService.execute(() -> walletDao.insert(wallet));
    }

    public LiveData<Double> getTotalBalance(boolean isBusiness) {
        return walletDao.getTotalBalance(isBusiness);
    }

    // Category methods
    public LiveData<List<Category>> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    public LiveData<List<Category>> getCategoriesByType(boolean isIncome) {
        return categoryDao.getCategoriesByType(isIncome);
    }

    public void insertCategory(Category category) {
        executorService.execute(() -> categoryDao.insert(category));
    }

    // Transaction methods
    public LiveData<List<Transaction>> getTransactions(boolean isBusiness) {
        return transactionDao.getTransactions(isBusiness);
    }

    public void insertTransaction(Transaction transaction) {
        executorService.execute(() -> {
            transactionDao.insert(transaction);
            // Logic Business: Cập nhật số dư ví và ngân sách khi có giao dịch mới
            handleTransactionLogic(transaction);
        });
    }

    private void handleTransactionLogic(Transaction tx) {
        // Đây là nơi xử lý Business Logic ngầm:
        // 1. Tìm ví tương ứng và cập nhật số dư (cần thêm method update balance vào WalletDao)
        // 2. Tìm ngân sách tương ứng (categoryId) và cập nhật số tiền đã chi
    }

    public LiveData<Double> getTotalIncome(boolean isBusiness) {
        return transactionDao.getTotalIncome(isBusiness);
    }

    public LiveData<Double> getTotalExpense(boolean isBusiness) {
        return transactionDao.getTotalExpense(isBusiness);
    }

    // Budget methods
    public LiveData<List<Budget>> getAllBudgets() {
        return budgetDao.getAllBudgets();
    }

    public void insertBudget(Budget budget) {
        executorService.execute(() -> budgetDao.insert(budget));
    }
}
