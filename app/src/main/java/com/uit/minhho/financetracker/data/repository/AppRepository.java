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

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppRepository {
    private final DecimalFormat moneyFormatter = new DecimalFormat("#,###");

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

    public void registerUser(User user) {
        executorService.execute(() -> userDao.registerUser(user));
    }

    public User login(String email, String password) {
        return userDao.login(email, password);
    }

    public LiveData<List<Wallet>> getWallets(boolean isBusiness) {
        return walletDao.getWallets(isBusiness);
    }

    public void insertWallet(Wallet wallet) {
        executorService.execute(() -> walletDao.insert(wallet));
    }

    public LiveData<Double> getTotalBalance(boolean isBusiness) {
        return walletDao.getTotalBalance(isBusiness);
    }

    public LiveData<List<Category>> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    public LiveData<List<Category>> getCategoriesByType(boolean isIncome) {
        return categoryDao.getCategoriesByType(isIncome);
    }

    public void insertCategory(Category category) {
        executorService.execute(() -> categoryDao.insert(category));
    }

    public LiveData<List<Transaction>> getTransactions(boolean isBusiness) {
        return transactionDao.getTransactions(isBusiness);
    }

    public void insertTransaction(Transaction transaction) {
        executorService.execute(() -> {
            if (transaction == null) {
                return;
            }
            if (transaction.isBusiness()) {
                saveBusinessTransactionSync(transaction);
            } else {
                transactionDao.insert(transaction);
            }
        });
    }

    public LiveData<List<Transaction>> getRecentTransactions(boolean isBusiness, int limit) {
        return transactionDao.getRecentTransactions(isBusiness, limit);
    }

    public LiveData<List<Transaction>> getTransactionsByPeriod(boolean isBusiness, long fromTimestamp, long toTimestamp) {
        return transactionDao.getTransactionsByPeriod(isBusiness, fromTimestamp, toTimestamp);
    }

    public LiveData<Double> getTotalIncome(boolean isBusiness) {
        return transactionDao.getTotalIncome(isBusiness);
    }

    public LiveData<Double> getTotalExpense(boolean isBusiness) {
        return transactionDao.getTotalExpense(isBusiness);
    }

    public LiveData<Double> getTotalIncomeByPeriod(boolean isBusiness, long fromTimestamp, long toTimestamp) {
        return transactionDao.getTotalIncomeByPeriod(isBusiness, fromTimestamp, toTimestamp);
    }

    public LiveData<Double> getTotalExpenseByPeriod(boolean isBusiness, long fromTimestamp, long toTimestamp) {
        return transactionDao.getTotalExpenseByPeriod(isBusiness, fromTimestamp, toTimestamp);
    }

    public LiveData<List<Budget>> getBudgets(boolean isBusiness) {
        return budgetDao.getBudgets(isBusiness);
    }

    public LiveData<List<Budget>> getAllBudgets() {
        return budgetDao.getAllBudgets();
    }

    public LiveData<List<Budget>> getBudgetsByPeriod(boolean isBusiness, String period) {
        return budgetDao.getBudgetsByPeriod(isBusiness, period);
    }

    public void insertBudget(Budget budget) {
        executorService.execute(() -> {
            if (budget.getName() == null) {
                budget.setName("");
            }
            budgetDao.insert(budget);
        });
    }

    public void createBusinessBudget(String budgetName, double limitAmount, double spentAmount, String period, SaveCallback callback) {
        executorService.execute(() -> {
            if (limitAmount <= 0) {
                if (callback != null) {
                    callback.onComplete(SaveResult.error("Han muc phai lon hon 0"));
                }
                return;
            }
            if (spentAmount < 0) {
                if (callback != null) {
                    callback.onComplete(SaveResult.error("So da dung khong hop le"));
                }
                return;
            }
            int categoryId = ensureBusinessCategory(budgetName, false);
            Budget budget = new Budget(budgetName, categoryId, limitAmount, spentAmount, period, true);
            budgetDao.insert(budget);
            if (callback != null) {
                callback.onComplete(SaveResult.success());
            }
        });
    }

    public void saveBusinessTransaction(Transaction transaction, SaveCallback callback) {
        executorService.execute(() -> {
            SaveResult result = saveBusinessTransactionSync(transaction);
            if (callback != null) {
                callback.onComplete(result);
            }
        });
    }

    public SaveResult createBusinessPayment(String receiver, String sourceAccount, double amount, String note, long timestamp) {
        Transaction tx = new Transaction(
                amount,
                timestamp,
                "Thanh toan den " + receiver + " | " + sourceAccount + " | " + note,
                ensureBusinessCategory("Thanh toan", false),
                resolveBusinessWalletId(),
                false,
                true
        );
        return saveBusinessTransactionSync(tx);
    }

    private SaveResult saveBusinessTransactionSync(Transaction tx) {
        if (tx == null) {
            return SaveResult.error("Giao dich khong hop le");
        }
        if (!tx.isBusiness()) {
            return SaveResult.error("Chi ho tro giao dich doanh nghiep");
        }
        if (tx.getAmount() <= 0) {
            return SaveResult.error("So tien phai lon hon 0");
        }

        int walletId = tx.getWalletId();
        if (walletId <= 0) {
            walletId = resolveBusinessWalletIdForAmount(tx.getAmount(), tx.isIncome());
            tx.setWalletId(walletId);
        }
        if (walletId <= 0) {
            return SaveResult.error("Chua co vi doanh nghiep");
        }

        if (tx.getCategoryId() <= 0) {
            String defaultCategory = tx.isIncome() ? "Doanh thu" : "Chi phi";
            tx.setCategoryId(ensureBusinessCategory(defaultCategory, tx.isIncome()));
        }

        Wallet wallet = walletDao.getWalletByIdSync(tx.getWalletId());
        if (wallet == null || !wallet.isBusiness()) {
            return SaveResult.error("Vi khong ton tai");
        }

        double currentBalance = wallet.getBalance();
        double nextBalance = tx.isIncome() ? currentBalance + tx.getAmount() : currentBalance - tx.getAmount();
        if (!tx.isIncome() && nextBalance < 0) {
            return SaveResult.error(
                    "So du vi khong du (con " + moneyFormatter.format(currentBalance)
                            + " d, can " + moneyFormatter.format(tx.getAmount())
                            + " d)."
            );
        }

        transactionDao.insert(tx);
        walletDao.updateBalanceById(wallet.getId(), nextBalance);

        if (!tx.isIncome()) {
            String period = resolvePeriodLabel(tx.getTimestamp());
            Budget budget = budgetDao.getBudgetByCategoryAndPeriodSync(tx.getCategoryId(), period);
            if (budget != null && budget.isBusiness()) {
                double nextSpent = budget.getSpentAmount() + tx.getAmount();
                budgetDao.updateSpentAmount(budget.getId(), nextSpent);
            }
        }

        return SaveResult.success();
    }

    private int resolveBusinessWalletId() {
        Wallet wallet = walletDao.getFirstWalletSync(true);
        if (wallet != null) {
            return wallet.getId();
        }
        Wallet defaultWallet = new Wallet("Vi doanh nghiep mac dinh", 0, "Tien mat", true);
        return (int) walletDao.insert(defaultWallet);
    }

    private int resolveBusinessWalletIdForAmount(double amount, boolean isIncome) {
        if (isIncome) {
            return resolveBusinessWalletId();
        }
        Wallet wallet = walletDao.getFirstWalletWithMinBalanceSync(true, amount);
        if (wallet != null) {
            return wallet.getId();
        }
        return resolveBusinessWalletId();
    }

    private int ensureBusinessCategory(String name, boolean isIncome) {
        Category existed = categoryDao.getCategoryByNameSync(name, isIncome);
        if (existed != null) {
            return existed.getId();
        }
        Category category = new Category(name, 0, "#4F46E5", isIncome);
        return (int) categoryDao.insert(category);
    }

    private String resolvePeriodLabel(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        int month = calendar.get(Calendar.MONTH) + 1;
        return calendar.get(Calendar.YEAR) + "-" + month;
    }

    public static final class SaveResult {
        private final boolean success;
        private final String message;

        private SaveResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static SaveResult success() {
            return new SaveResult(true, "");
        }

        public static SaveResult error(String message) {
            return new SaveResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    public interface SaveCallback {
        void onComplete(SaveResult result);
    }
}
