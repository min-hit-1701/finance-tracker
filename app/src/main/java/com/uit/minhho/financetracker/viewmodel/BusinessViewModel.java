package com.uit.minhho.financetracker.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.uit.minhho.financetracker.data.local.entity.Budget;
import com.uit.minhho.financetracker.data.local.entity.BusinessEntity;
import com.uit.minhho.financetracker.data.local.entity.Partner;
import com.uit.minhho.financetracker.data.local.entity.Transaction;
import com.uit.minhho.financetracker.data.local.entity.Wallet;
import com.uit.minhho.financetracker.data.repository.AppRepository;

import java.util.List;

public class BusinessViewModel extends AndroidViewModel {
    public static final String EVENT_BUSINESS_TRANSACTION_SAVED = "event_business_transaction_saved";

    private final AppRepository repository;
    private final MutableLiveData<String> operationMessage = new MutableLiveData<>();

    public BusinessViewModel(@NonNull Application application) {
        super(application);
        repository = new AppRepository(application);
    }

    public LiveData<List<BusinessEntity>> getBusinesses() {
        return repository.getBusinesses();
    }

    public LiveData<List<Partner>> getBusinessPartners() {
        return repository.getPartners(true);
    }

    public LiveData<List<Wallet>> getBusinessWallets() {
        return repository.getWallets(true);
    }

    public LiveData<List<Budget>> getBusinessBudgets() {
        return repository.getBudgets(true);
    }

    public LiveData<List<Transaction>> getBusinessTransactions() {
        return repository.getTransactions(true);
    }

    public LiveData<List<Transaction>> getRecentBusinessTransactions(int limit) {
        return repository.getRecentTransactions(true, limit);
    }

    public LiveData<List<Transaction>> getBusinessTransactionsByPeriod(long fromTimestamp, long toTimestamp) {
        return repository.getTransactionsByPeriod(true, fromTimestamp, toTimestamp);
    }

    public LiveData<Double> getBusinessTotalBalance() {
        return repository.getTotalBalance(true);
    }

    public LiveData<Double> getBusinessIncome() {
        return repository.getTotalIncome(true);
    }

    public LiveData<Double> getBusinessExpense() {
        return repository.getTotalExpense(true);
    }

    public LiveData<Double> getBusinessIncomeByPeriod(long fromTimestamp, long toTimestamp) {
        return repository.getTotalIncomeByPeriod(true, fromTimestamp, toTimestamp);
    }

    public LiveData<Double> getBusinessExpenseByPeriod(long fromTimestamp, long toTimestamp) {
        return repository.getTotalExpenseByPeriod(true, fromTimestamp, toTimestamp);
    }

    public LiveData<String> getOperationMessage() {
        return operationMessage;
    }

    public void clearOperationMessage() {
        operationMessage.postValue(null);
    }

    public void addBusiness(String name, String type, String taxCode, String note) {
        repository.insertBusiness(new BusinessEntity(name, type, taxCode, note));
        operationMessage.postValue("Đã lưu doanh nghiệp vào danh sách");
    }

    public void addBusinessWallet(String name, double balance, String type, String note) {
        Wallet wallet = new Wallet(name, balance, type + " | " + note, true);
        repository.insertWallet(wallet);
        operationMessage.postValue("Đã thêm ví doanh nghiệp");
    }

    public void addBusinessBudget(String budgetName, double limitAmount, double usedAmount, String period) {
        repository.createBusinessBudget(budgetName, limitAmount, usedAmount, period, result -> {
            if (result.isSuccess()) {
                operationMessage.postValue("Đã thêm ngân sách mới");
            } else {
                operationMessage.postValue(result.getMessage());
            }
        });
    }

    public void addBusinessTransaction(Transaction transaction) {
        repository.saveBusinessTransaction(transaction, result -> {
            if (result.isSuccess()) {
                operationMessage.postValue(EVENT_BUSINESS_TRANSACTION_SAVED);
            } else {
                operationMessage.postValue(result.getMessage());
            }
        });
    }

    public void createBusinessPayment(String receiver, String sourceAccount, double amount, String note, long timestamp) {
        new Thread(() -> {
            AppRepository.SaveResult result = repository.createBusinessPayment(receiver, sourceAccount, amount, note, timestamp);
            if (result.isSuccess()) {
                operationMessage.postValue("Đã tạo lệnh chi thành công");
            } else {
                operationMessage.postValue(result.getMessage());
            }
        }).start();
    }
}
