package com.uit.minhho.financetracker.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.uit.minhho.financetracker.data.local.entity.Budget;
import com.uit.minhho.financetracker.data.local.entity.Category;
import com.uit.minhho.financetracker.data.local.entity.Transaction;
import com.uit.minhho.financetracker.data.local.entity.User;
import com.uit.minhho.financetracker.data.local.entity.Wallet;
import com.uit.minhho.financetracker.data.remote.AuthApiClient;
import com.uit.minhho.financetracker.data.remote.PersonalApiClient;

import java.util.ArrayList;
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
        loadWallets(false);
        loadCategories(null);
        loadTransactions(false);
        loadBudgets();
        loadTotals();
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
        loadWallets(isBusiness);
        return isBusiness ? businessWallets : personalWallets;
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
                postBusinessPlaceholder(callback);
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
                postBusinessPlaceholder(callback);
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
        loadTotalBalance(isBusiness);
        return isBusiness ? businessTotalBalance : personalTotalBalance;
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
        loadTransactions(isBusiness);
        return isBusiness ? businessTransactions : personalTransactions;
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

    public LiveData<Double> getTotalIncome(boolean isBusiness) {
        loadTotalAmount(isBusiness, true);
        return isBusiness ? businessTotalIncome : personalTotalIncome;
    }

    public LiveData<Double> getTotalExpense(boolean isBusiness) {
        loadTotalAmount(isBusiness, false);
        return isBusiness ? businessTotalExpense : personalTotalExpense;
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
}
