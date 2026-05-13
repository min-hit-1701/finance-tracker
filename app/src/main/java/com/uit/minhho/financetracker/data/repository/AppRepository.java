package com.uit.minhho.financetracker.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.uit.minhho.financetracker.data.local.dao.BudgetDao;
import com.uit.minhho.financetracker.data.local.dao.BusinessDao;
import com.uit.minhho.financetracker.data.local.dao.CategoryDao;
import com.uit.minhho.financetracker.data.local.dao.PartnerDao;
import com.uit.minhho.financetracker.data.local.dao.TransactionDao;
import com.uit.minhho.financetracker.data.local.dao.UserDao;
import com.uit.minhho.financetracker.data.local.dao.WalletDao;
import com.uit.minhho.financetracker.data.local.database.AppDatabase;
import com.uit.minhho.financetracker.data.local.entity.Budget;
import com.uit.minhho.financetracker.data.local.entity.BusinessEntity;
import com.uit.minhho.financetracker.data.local.entity.Category;
import com.uit.minhho.financetracker.data.local.entity.Partner;
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
    private final PartnerDao partnerDao;
    private final BusinessDao businessDao;
    private final ExecutorService executorService;

    public AppRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        walletDao = db.walletDao();
        categoryDao = db.categoryDao();
        transactionDao = db.transactionDao();
        budgetDao = db.budgetDao();
        partnerDao = db.partnerDao();
        businessDao = db.businessDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    public void registerUser(User user) {
        executorService.execute(() -> userDao.registerUser(user));
    }

    public User login(String email, String password) {
        return userDao.login(email, password);
    }

    public LiveData<List<BusinessEntity>> getBusinesses() {
        return businessDao.getBusinesses();
    }

    public void insertBusiness(BusinessEntity business) {
        executorService.execute(() -> businessDao.insert(business));
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

    public LiveData<List<Partner>> getPartners(boolean isBusiness) {
        return partnerDao.getPartners(isBusiness);
    }

    public void insertPartner(Partner partner) {
        executorService.execute(() -> partnerDao.insert(partner));
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
                notifySave(callback, SaveResult.error("Hạn mức phải lớn hơn 0"));
                return;
            }
            if (spentAmount < 0) {
                notifySave(callback, SaveResult.error("Số đã dùng không hợp lệ"));
                return;
            }
            int categoryId = ensureBusinessCategory(budgetName, false);
            Budget budget = new Budget(budgetName, categoryId, limitAmount, spentAmount, period, true);
            budgetDao.insert(budget);
            notifySave(callback, SaveResult.success());
        });
    }

    public void saveBusinessTransaction(Transaction transaction, SaveCallback callback) {
        executorService.execute(() -> notifySave(callback, saveBusinessTransactionSync(transaction)));
    }

    public SaveResult createBusinessPayment(String receiver, String sourceAccount, double amount, String note, long timestamp) {
        int walletId = resolveBusinessWalletIdForAmount(amount, false);
        if (walletId <= 0) {
            return SaveResult.error("Chưa có ví doanh nghiệp đủ số dư");
        }

        Transaction tx = new Transaction(
                amount,
                timestamp,
                "Thanh toán đến " + receiver + " | " + sourceAccount + " | " + note,
                ensureBusinessCategory("Thanh toán", false),
                walletId,
                ensureBusinessPartner(receiver),
                false,
                true
        );
        return saveBusinessTransactionSync(tx);
    }

    private SaveResult saveBusinessTransactionSync(Transaction tx) {
        if (tx == null) {
            return SaveResult.error("Giao dịch không hợp lệ");
        }
        if (!tx.isBusiness()) {
            return SaveResult.error("Chỉ hỗ trợ giao dịch doanh nghiệp");
        }
        if (tx.getAmount() <= 0) {
            return SaveResult.error("Số tiền phải lớn hơn 0");
        }
        if (tx.getWalletId() <= 0) {
            return SaveResult.error("Vui lòng chọn ví doanh nghiệp");
        }

        if (tx.getCategoryId() <= 0) {
            String defaultCategory = tx.isIncome() ? "Doanh thu" : "Chi phí";
            String categoryName = resolveNotePart(tx.getNote(), 0, defaultCategory);
            tx.setCategoryId(ensureBusinessCategory(categoryName, tx.isIncome()));
        }

        if (tx.getPartnerId() <= 0) {
            String partnerName = resolveNotePart(tx.getNote(), 1, "");
            if (!partnerName.isEmpty()) {
                tx.setPartnerId(ensureBusinessPartner(partnerName));
            }
        }

        Wallet wallet = walletDao.getWalletByIdSync(tx.getWalletId());
        if (wallet == null || !wallet.isBusiness()) {
            return SaveResult.error("Ví không tồn tại");
        }

        double currentBalance = wallet.getBalance();
        double nextBalance = tx.isIncome() ? currentBalance + tx.getAmount() : currentBalance - tx.getAmount();
        if (!tx.isIncome() && nextBalance < 0) {
            return SaveResult.error(
                    "Số dư ví không đủ (còn " + moneyFormatter.format(currentBalance)
                            + " đ, cần " + moneyFormatter.format(tx.getAmount())
                            + " đ)."
            );
        }

        transactionDao.insert(tx);
        walletDao.updateBalanceById(wallet.getId(), nextBalance);

        if (!tx.isIncome()) {
            String period = resolvePeriodLabel(tx.getTimestamp());
            Budget budget = budgetDao.getBudgetByCategoryAndPeriodSync(tx.getCategoryId(), period);
            if (budget != null && budget.isBusiness()) {
                budgetDao.updateSpentAmount(budget.getId(), budget.getSpentAmount() + tx.getAmount());
            }
        }

        return SaveResult.success();
    }

    private int resolveBusinessWalletIdForAmount(double amount, boolean isIncome) {
        Wallet wallet = isIncome
                ? walletDao.getFirstWalletSync(true)
                : walletDao.getFirstWalletWithMinBalanceSync(true, amount);
        return wallet == null ? 0 : wallet.getId();
    }

    private int ensureBusinessCategory(String name, boolean isIncome) {
        String normalizedName = name == null || name.trim().isEmpty()
                ? (isIncome ? "Doanh thu" : "Chi phí")
                : name.trim();
        Category existed = categoryDao.getCategoryByNameSync(normalizedName, isIncome);
        if (existed != null) {
            return existed.getId();
        }
        Category category = new Category(normalizedName, 0, "#4F46E5", isIncome);
        return (int) categoryDao.insert(category);
    }

    private int ensureBusinessPartner(String name) {
        String normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isEmpty()) {
            return 0;
        }
        Partner existed = partnerDao.getPartnerByNameSync(normalizedName, true);
        if (existed != null) {
            return existed.getId();
        }
        Partner partner = new Partner(normalizedName, "", "", "", "Đối tác", true);
        return (int) partnerDao.insert(partner);
    }

    private String resolveNotePart(String note, int index, String fallback) {
        if (note == null || note.trim().isEmpty()) {
            return fallback;
        }
        String[] parts = note.split("\\|");
        if (parts.length <= index) {
            return fallback;
        }
        String value = parts[index].trim();
        return value.isEmpty() ? fallback : value;
    }

    private String resolvePeriodLabel(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        int month = calendar.get(Calendar.MONTH) + 1;
        return calendar.get(Calendar.YEAR) + "-" + month;
    }

    private void notifySave(SaveCallback callback, SaveResult result) {
        if (callback != null) {
            callback.onComplete(result);
        }
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
