package com.uit.minhho.financetracker.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.uit.minhho.financetracker.data.local.entity.Transaction;
import com.uit.minhho.financetracker.data.repository.AppRepository;
import java.util.List;

public class TransactionViewModel extends AndroidViewModel {
    private final AppRepository repository;

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        repository = new AppRepository(application);
    }

    public LiveData<List<Transaction>> getTransactions(boolean isBusiness) {
        return repository.getTransactions(isBusiness);
    }

    public void refreshTransactions(boolean isBusiness) {
        repository.refreshTransactions(isBusiness);
    }

    public void insert(Transaction transaction) {
        repository.insertTransaction(transaction);
    }

    public void insert(Transaction transaction, AppRepository.OperationCallback callback) {
        repository.insertTransaction(transaction, callback);
    }

    public void delete(Transaction transaction, AppRepository.OperationCallback callback) {
        repository.deleteTransaction(transaction, callback);
    }

    public LiveData<Double> getTotalIncome(boolean isBusiness) {
        return repository.getTotalIncome(isBusiness);
    }

    public LiveData<Double> getTotalExpense(boolean isBusiness) {
        return repository.getTotalExpense(isBusiness);
    }
}
