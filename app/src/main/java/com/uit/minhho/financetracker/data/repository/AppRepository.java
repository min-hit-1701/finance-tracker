package com.uit.minhho.financetracker.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.uit.minhho.financetracker.data.local.entity.Budget;
import com.uit.minhho.financetracker.data.local.entity.BusinessContact;
import com.uit.minhho.financetracker.data.local.entity.Category;
import com.uit.minhho.financetracker.data.local.entity.Partner;
import com.uit.minhho.financetracker.data.local.entity.Transaction;
import com.uit.minhho.financetracker.data.local.entity.User;
import com.uit.minhho.financetracker.data.local.entity.Wallet;
import com.uit.minhho.financetracker.data.local.dao.BusinessContactDao;
import com.uit.minhho.financetracker.data.local.dao.PartnerDao;
import com.uit.minhho.financetracker.data.local.database.AppDatabase;
import com.uit.minhho.financetracker.data.remote.AuthApiClient;
import com.uit.minhho.financetracker.data.remote.BusinessApiClient;
import com.uit.minhho.financetracker.data.remote.PersonalApiClient;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppRepository {
    public interface OperationCallback {
        void onComplete(boolean success, String message);
    }

    private final ExecutorService executorService;
    private final AuthApiClient authApiClient;
    private final PersonalApiClient personalApiClient;
    private final BusinessApiClient businessApiClient;
    private final BusinessContactDao businessContactDao;
    private final PartnerDao partnerDao;
    private final com.uit.minhho.financetracker.data.local.dao.WalletDao walletDao;
    private final com.uit.minhho.financetracker.data.local.dao.TransactionDao transactionDao;
    private final com.uit.minhho.financetracker.data.local.dao.CategoryDao categoryDao;
    private final com.uit.minhho.financetracker.data.local.dao.BudgetDao budgetDao;
    private final DecimalFormat moneyFormatter = new DecimalFormat("#,###");
    private final MutableLiveData<List<Wallet>> personalWallets = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Wallet>> businessWallets = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Map<Integer, Double>> categoryUsageTotals = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<List<Transaction>> personalTransactions = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Transaction>> businessTransactions = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Budget>> budgets = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Double> personalTotalBalance = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> businessTotalBalance = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> personalTotalIncome = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> businessTotalIncome = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> personalTotalExpense = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> businessTotalExpense = new MutableLiveData<>(0.0);

    public AppRepository(Application application) {
        executorService = Executors.newFixedThreadPool(4);
        authApiClient = new AuthApiClient();
        personalApiClient = new PersonalApiClient(application);
        businessApiClient = new BusinessApiClient(application.getApplicationContext());
        AppDatabase db = AppDatabase.getDatabase(application);
        businessContactDao = db.businessContactDao();
        partnerDao = db.partnerDao();
        walletDao = db.walletDao();
        transactionDao = db.transactionDao();
        categoryDao = db.categoryDao();
        budgetDao = db.budgetDao();
        loadWallets(false);
        loadCategories(null);
        loadTransactions(false);
        loadBudgets();
        loadTotals();
        syncBusinessFromFirebase();
    }

    public void registerUser(User user) {
        executorService.execute(() -> authApiClient.register(
                user.getFullName(),
                user.getEmail(),
                user.getPassword()
        ));
    }

    public User login(String email, String password) {
        AuthApiClient.AuthResult result = authApiClient.login(email, password);
        if (!result.success) {
            return null;
        }
        User user = new User("", email, password);
        user.setId(result.userId);
        return user;
    }

    public LiveData<List<Wallet>> getWallets(boolean isBusiness) {
        if (isBusiness) {
            return walletDao.getWallets(true);
        }
        loadWallets(false);
        return personalWallets;
    }

    public void refreshWallets(boolean isBusiness) {
        loadWallets(isBusiness);
        loadTotalBalance(isBusiness);
    }

    public void insertWallet(Wallet wallet) {
        insertWallet(wallet, null);
    }

    public void insertWallet(Wallet wallet, OperationCallback callback) {
        executorService.execute(() -> {
            if (wallet.isBusiness()) {
                walletDao.insert(wallet);
                businessApiClient.createWallet(wallet.getName(), wallet.getBalance(), wallet.getType());
                notify(callback, true, "Đã thêm ví doanh nghiệp");
                return;
            }

            PersonalApiClient.ApiResult<Wallet> result = personalApiClient.createWallet(wallet);
            if (result.success) {
                loadWallets(false);
                loadTotals();
            }
            notify(callback, result.success, result.message);
        });
    }

    public void deleteWallet(Wallet wallet) {
        deleteWallet(wallet, null);
    }

    public void deleteWallet(Wallet wallet, OperationCallback callback) {
        executorService.execute(() -> {
            if (wallet.isBusiness()) {
                walletDao.delete(wallet);
                notify(callback, true, "Đã xóa ví doanh nghiệp");
                return;
            }

            PersonalApiClient.ApiResult<Void> result = personalApiClient.deleteWallet(wallet);
            if (result.success) {
                loadWallets(false);
                loadTotals();
            }
            notify(callback, result.success, result.message);
        });
    }

    public LiveData<Double> getTotalBalance(boolean isBusiness) {
        if (isBusiness) {
            return walletDao.getTotalBalance(true);
        }
        loadTotalBalance(false);
        return personalTotalBalance;
    }

    public LiveData<Double> getTotalIncome(boolean isBusiness) {
        if (isBusiness) {
            return transactionDao.getTotalIncome(true);
        }
        loadTotalAmount(false, true);
        return personalTotalIncome;
    }

    public LiveData<Double> getTotalExpense(boolean isBusiness) {
        if (isBusiness) {
            return transactionDao.getTotalExpense(true);
        }
        loadTotalAmount(false, false);
        return personalTotalExpense;
    }

    private void loadWallets(boolean isBusiness) {
        executorService.execute(() -> {
            if (isBusiness) {
                businessWallets.postValue(new ArrayList<>());
                return;
            }

            PersonalApiClient.ApiResult<List<Wallet>> result = personalApiClient.getWallets();
            if (result.success && result.data != null) {
                personalWallets.postValue(result.data);
            }
        });
    }

    public LiveData<List<Category>> getAllCategories() {
        loadCategories(null);
        return categories;
    }

    public void refreshCategories() {
        loadCategories(null);
        loadCategoryUsageTotals(categoryUsageTotals);
    }

    public LiveData<List<Category>> getCategoriesByType(boolean isIncome) {
        MutableLiveData<List<Category>> result = new MutableLiveData<>(new ArrayList<>());
        loadCategories(isIncome, result);
        return result;
    }

    public void insertCategory(Category category) {
        insertCategory(category, null);
    }

    public void insertCategory(Category category, OperationCallback callback) {
        executorService.execute(() -> {
            PersonalApiClient.ApiResult<Category> result = personalApiClient.createCategory(category);
            if (result.success) {
                loadCategories(null);
            }
            notify(callback, result.success, result.message);
        });
    }

    private void loadCategories(Boolean isIncome) {
        loadCategories(isIncome, categories);
    }

    private void loadCategories(Boolean isIncome, MutableLiveData<List<Category>> target) {
        executorService.execute(() -> {
            PersonalApiClient.ApiResult<List<Category>> result = personalApiClient.getCategories(isIncome);
            if (result.success && result.data != null) {
                target.postValue(result.data);
            }
        });
    }

    public LiveData<Map<Integer, Double>> getCategoryUsageTotals() {
        loadCategoryUsageTotals(categoryUsageTotals);
        return categoryUsageTotals;
    }

    private void loadCategoryUsageTotals(MutableLiveData<Map<Integer, Double>> target) {
        executorService.execute(() -> {
            Map<Integer, Double> totals = new HashMap<>();
            PersonalApiClient.ApiResult<List<Transaction>> apiResult = personalApiClient.getTransactions();
            if (apiResult.success && apiResult.data != null) {
                for (Transaction transaction : apiResult.data) {
                    int categoryId = transaction.getCategoryId();
                    if (categoryId > 0) {
                        double current = totals.containsKey(categoryId) ? totals.get(categoryId) : 0.0;
                        totals.put(categoryId, current + transaction.getAmount());
                    }
                }
            }
            target.postValue(totals);
        });
    }

    public LiveData<List<Transaction>> getTransactions(boolean isBusiness) {
        if (isBusiness) {
            return transactionDao.getTransactions(true);
        }
        loadTransactions(false);
        return personalTransactions;
    }

    public void refreshTransactions(boolean isBusiness) {
        loadTransactions(isBusiness);
        loadTotals();
    }

    public void insertTransaction(Transaction transaction) {
        insertTransaction(transaction, null);
    }

    public void insertTransaction(Transaction transaction, OperationCallback callback) {
        executorService.execute(() -> {
            if (transaction.isBusiness()) {
                postBusinessPlaceholder(callback);
                return;
            }

            PersonalApiClient.ApiResult<Transaction> result = personalApiClient.createTransaction(transaction);
            if (result.success) {
                loadTransactions(false);
                loadWallets(false);
                loadTotals();
            }
            notify(callback, result.success, result.message);
        });
    }

    public void deleteTransaction(Transaction transaction, OperationCallback callback) {
        executorService.execute(() -> {
            if (transaction.isBusiness()) {
                postBusinessPlaceholder(callback);
                return;
            }

            PersonalApiClient.ApiResult<Void> result = personalApiClient.deleteTransaction(transaction);
            if (result.success) {
                loadTransactions(false);
                loadWallets(false);
                loadTotals();
                loadCategoryUsageTotals(categoryUsageTotals);
            }
            notify(callback, result.success, result.message);
        });
    }

    private void loadTransactions(boolean isBusiness) {
        executorService.execute(() -> {
            if (isBusiness) {
                businessTransactions.postValue(new ArrayList<>());
                return;
            }

            PersonalApiClient.ApiResult<List<Transaction>> result = personalApiClient.getTransactions();
            if (result.success && result.data != null) {
                personalTransactions.postValue(result.data);
            }
        });
    }

    public LiveData<List<Budget>> getAllBudgets() {
        loadBudgets();
        return budgets;
    }

    public void refreshBudgets() {
        loadBudgets();
    }

    public void insertBudget(Budget budget) {
        insertBudget(budget, null);
    }

    public void insertBudget(Budget budget, OperationCallback callback) {
        executorService.execute(() -> {
            PersonalApiClient.ApiResult<Budget> result = personalApiClient.createBudget(budget);
            if (result.success) {
                loadBudgets();
            }
            notify(callback, result.success, result.message);
        });
    }

    public void deleteBudget(Budget budget, OperationCallback callback) {
        executorService.execute(() -> {
            PersonalApiClient.ApiResult<Void> result = personalApiClient.deleteBudget(budget);
            if (result.success) {
                loadBudgets();
            }
            notify(callback, result.success, result.message);
        });
    }

    private void loadBudgets() {
        executorService.execute(() -> {
            PersonalApiClient.ApiResult<List<Budget>> result = personalApiClient.getBudgets();
            budgets.postValue(result.success && result.data != null ? result.data : new ArrayList<>());
        });
    }

    private void loadTotals() {
        loadTotalBalance(false);
        loadTotalAmount(false, true);
        loadTotalAmount(false, false);
    }

    private void loadTotalBalance(boolean isBusiness) {
        executorService.execute(() -> {
            if (isBusiness) {
                businessTotalBalance.postValue(0.0);
                return;
            }

            PersonalApiClient.ApiResult<PersonalApiClient.Summary> result = personalApiClient.getSummary();
            if (result.success && result.data != null) {
                personalTotalBalance.postValue(result.data.totalBalance);
            }
        });
    }

    private void loadTotalAmount(boolean isBusiness, boolean isIncome) {
        executorService.execute(() -> {
            if (isBusiness) {
                if (isIncome) {
                    businessTotalIncome.postValue(0.0);
                } else {
                    businessTotalExpense.postValue(0.0);
                }
                return;
            }

            PersonalApiClient.ApiResult<PersonalApiClient.Summary> result = personalApiClient.getSummary();
            if (result.success && result.data != null) {
                if (isIncome) {
                    personalTotalIncome.postValue(result.data.totalIncome);
                } else {
                    personalTotalExpense.postValue(result.data.totalExpense);
                }
            }
        });
    }

    private void notify(OperationCallback callback, boolean success, String message) {
        if (callback != null) {
            callback.onComplete(success, message);
        }
    }

    private void postBusinessPlaceholder(OperationCallback callback) {
        notify(callback, false, "Business đang dùng BusinessApiClient Firebase riêng");
    }

    // ===== BUSINESS ROOM METHODS =====

    public LiveData<List<Budget>> getBudgets(boolean isBusiness) {
        return budgetDao.getBudgets(isBusiness);
    }

    public LiveData<List<Transaction>> getRecentTransactions(boolean isBusiness, int limit) {
        return transactionDao.getRecentTransactions(isBusiness, limit);
    }

    public LiveData<List<Transaction>> getTransactionsByPeriod(boolean isBusiness, long from, long to) {
        return transactionDao.getTransactionsByPeriod(isBusiness, from, to);
    }

    public LiveData<Double> getTotalIncomeByPeriod(boolean isBusiness, long from, long to) {
        return transactionDao.getTotalIncomeByPeriod(isBusiness, from, to);
    }

    public LiveData<Double> getTotalExpenseByPeriod(boolean isBusiness, long from, long to) {
        return transactionDao.getTotalExpenseByPeriod(isBusiness, from, to);
    }

    public LiveData<List<Partner>> getPartners(boolean isBusiness) {
        return partnerDao.getPartners(isBusiness);
    }

    public LiveData<List<BusinessContact>> getAllBusinessContacts() {
        return businessContactDao.getAllContacts();
    }

    public List<BusinessContact> getAllBusinessContactsSync() {
        return businessContactDao.getAllContactsSync();
    }

    public void insertBusinessContact(BusinessContact contact) {
        executorService.execute(() -> businessContactDao.insert(contact));
    }

    public long insertBusinessContactSync(BusinessContact contact) {
        long id = businessContactDao.insert(contact);
        businessApiClient.createBusinessEntity(contact.getName(), contact.getType(), contact.getNote());
        return id;
    }

    public void deleteBusinessContactSync(BusinessContact contact) {
        businessContactDao.delete(contact);
    }

    public void deleteBusinessBudgetSync(Budget budget) {
        budgetDao.delete(budget);
    }

    public void updateBusinessBudgetSync(int id, String name, double limit, double used, String period) {
        budgetDao.updateBudget(id, name, limit, used, period);
    }

    public void deleteBusinessTransactionSync(Transaction transaction) {
        if (transaction.getId() > 0) {
            Transaction existing = transactionDao.getTransactionByIdSync(transaction.getId());
            if (existing != null) {
                transactionDao.delete(existing);
                double delta = existing.isIncome() ? -existing.getAmount() : existing.getAmount();
                walletDao.updateBalanceById(existing.getWalletId(), walletDao.getWalletByIdSync(existing.getWalletId()).getBalance() + delta);
            }
        }
    }

    public void saveBusinessTransaction(Transaction transaction, SaveCallback callback) {
        executorService.execute(() -> {
            SaveResult result = saveBusinessTransactionSync(transaction);
            if (callback != null) callback.onComplete(result);
        });
    }

    public void createBusinessBudget(String name, double limit, double spent, String period, SaveCallback callback) {
        executorService.execute(() -> {
            if (limit <= 0) { callback.onComplete(SaveResult.error("Han muc phai lon hon 0")); return; }
            if (spent < 0) { callback.onComplete(SaveResult.error("So da dung khong hop le")); return; }
            int catId = ensureBusinessCategory(name, false);
            Budget b = new Budget(name, catId, limit, spent, period, true);
            budgetDao.insert(b);
            callback.onComplete(SaveResult.success());
        });
    }

    public SaveResult createBusinessPayment(String receiver, String account, double amount, String note, long time, int walletId, int partnerId) {
        Transaction tx = new Transaction(amount, time, "Thanh toan den " + receiver, ensureBusinessCategory("Thanh toan", false), walletId > 0 ? walletId : resolveBusinessWalletId(), false, true, 0);
        tx.setPartnerId(partnerId);
        return saveBusinessTransactionSync(tx);
    }

    private SaveResult saveBusinessTransactionSync(Transaction tx) {
        if (tx == null) return SaveResult.error("Giao dich khong hop le");
        if (!tx.isBusiness()) return SaveResult.error("Chi ho tro giao dich doanh nghiep");
        if (tx.getAmount() <= 0) return SaveResult.error("So tien phai lon hon 0");
        int walletId = tx.getWalletId();
        if (walletId <= 0) { walletId = resolveBusinessWalletIdForAmount(tx.getAmount(), tx.isIncome()); tx.setWalletId(walletId); }
        if (walletId <= 0) return SaveResult.error("Chua co vi doanh nghiep");
        if (tx.getCategoryId() <= 0) {
            String catName = tx.isIncome() ? "Doanh thu" : "Chi phi";
            if (tx.getNote() != null && tx.getNote().contains(" | ")) {
                String[] parts = tx.getNote().split(" \\| ");
                if (parts.length >= 1) catName = parts[0];
            }
            tx.setCategoryId(ensureBusinessCategory(catName, tx.isIncome()));
        }
        Wallet wallet = walletDao.getWalletByIdSync(tx.getWalletId());
        if (wallet == null || !wallet.isBusiness()) return SaveResult.error("Vi khong ton tai");
        double current = wallet.getBalance();
        double next = tx.isIncome() ? current + tx.getAmount() : current - tx.getAmount();
        if (!tx.isIncome() && next < 0) return SaveResult.error("So du vi khong du (con " + moneyFormatter.format(current) + " d, can " + moneyFormatter.format(tx.getAmount()) + " d)");
        transactionDao.insert(tx);
        walletDao.updateBalanceById(wallet.getId(), next);
        syncTransactionToFirebase(tx);
        String period = resolvePeriodLabel(tx.getTimestamp());
        String catName = tx.getNote() != null && tx.getNote().contains(" | ")
                ? tx.getNote().split(" \\| ")[0] : "";
        List<Budget> allBudgets = budgetDao.getBudgetsByPeriodSync(period);
        Budget matched = null;
        for (Budget b : allBudgets) {
            if (b.getName() != null && b.getName().equalsIgnoreCase(catName)) {
                matched = b;
                break;
            }
        }
        if (matched != null) {
            if (tx.isIncome()) {
                matched.setSpentAmount(Math.max(0, matched.getSpentAmount() - tx.getAmount()));
                budgetDao.update(matched);
            } else {
                double newSpent = matched.getSpentAmount() + tx.getAmount();
                matched.setSpentAmount(newSpent);
                budgetDao.update(matched);
                if (newSpent > matched.getLimitAmount()) {
                    return SaveResult.warn("Canh bao: Da vuot han muc ngan sach \"" + matched.getName() + "\"! (Da dung " + moneyFormatter.format(newSpent) + " d / " + moneyFormatter.format(matched.getLimitAmount()) + " d)");
                }
            }
        }
        return SaveResult.success();
    }

    private int resolveBusinessWalletId() {
        Wallet w = walletDao.getFirstWalletSync(true);
        if (w != null) return w.getId();
        return (int) walletDao.insert(new Wallet("Vi doanh nghiep mac dinh", 0, "Tien mat", true));
    }

    private int resolveBusinessWalletIdForAmount(double amount, boolean isIncome) {
        if (isIncome) return resolveBusinessWalletId();
        Wallet w = walletDao.getFirstWalletWithMinBalanceSync(true, amount);
        return w != null ? w.getId() : resolveBusinessWalletId();
    }

    private void syncTransactionToFirebase(Transaction tx) {
        try {
            String partner = "";
            String category = tx.isIncome() ? "Doanh thu" : "Chi phi";
            if (tx.getNote() != null && tx.getNote().contains(" | ")) {
                String[] parts = tx.getNote().split(" \\| ");
                if (parts.length >= 2) { category = parts[0]; partner = parts[1]; }
            }
            String ts = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                    .format(new java.util.Date(tx.getTimestamp()));
            businessApiClient.createTransaction(tx.getAmount(), partner, category, ts,
                    tx.getNote() != null ? tx.getNote() : "", tx.isIncome());
        } catch (Exception ignored) {}
    }

    private int ensureBusinessCategory(String name, boolean isIncome) {
        Category existed = categoryDao.getCategoryByNameSync(name, isIncome);
        if (existed != null) return existed.getId();
        return (int) categoryDao.insert(new Category(name, 0, "#4F46E5", isIncome));
    }

    private String resolvePeriodLabel(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1);
    }

    public static final class SaveResult {
        private final boolean success;
        private final String message;
        private final boolean warning;
        private SaveResult(boolean s, String m, boolean w) { success = s; message = m; warning = w; }
        public static SaveResult success() { return new SaveResult(true, "", false); }
        public static SaveResult warn(String m) { return new SaveResult(true, m, true); }
        public static SaveResult error(String m) { return new SaveResult(false, m, false); }
        public boolean isSuccess() { return success; }
        public boolean isWarning() { return warning; }
        public String getMessage() { return message; }
    }

    public interface SaveCallback { void onComplete(SaveResult result); }

    private void syncBusinessFromFirebase() {
        executorService.execute(() -> {
            try {
                List<com.uit.minhho.financetracker.model.business.BusinessWallet> fbWallets = businessApiClient.getWallets();
                if (fbWallets != null) {
                    for (com.uit.minhho.financetracker.model.business.BusinessWallet fw : fbWallets) {
                        double balance = 0;
                        try { balance = Double.parseDouble(fw.getBalance().replaceAll("[^0-9.]", "")); } catch (Exception ignored) {}
                        Wallet existing = walletDao.getWalletByNameSync(fw.getName(), true);
                        if (existing == null) {
                            walletDao.insert(new Wallet(fw.getName(), balance, fw.getNote() != null ? fw.getNote() : "Doanh nghiệp", true));
                        }
                    }
                }

                List<com.uit.minhho.financetracker.model.business.BusinessEntity> fbContacts = businessApiClient.getBusinessEntities();
                if (fbContacts != null) {
                    for (com.uit.minhho.financetracker.model.business.BusinessEntity fe : fbContacts) {
                        List<BusinessContact> existing = businessContactDao.searchContactsSync(fe.getName());
                        if (existing == null || existing.isEmpty()) {
                            businessContactDao.insert(new BusinessContact(fe.getName(), fe.getType(), fe.getNote()));
                        }
                    }
                }

                List<com.uit.minhho.financetracker.model.business.BusinessTransaction> fbTxns = businessApiClient.getTransactions();
                if (fbTxns != null) {
                    for (com.uit.minhho.financetracker.model.business.BusinessTransaction ft : fbTxns) {
                        if (ft.getId() > 0 && transactionDao.getTransactionByIdSync(ft.getId()) == null) {
                            Transaction tx = new Transaction(ft.getRawAmount(), ft.getTimestamp(),
                                    ft.getTitle() != null ? ft.getTitle() : "",
                                    0, 0, ft.isIncome(), true, 0);
                            transactionDao.insert(tx);
                        }
                    }
                }
            } catch (Exception ignored) {}
        });
    }
}
