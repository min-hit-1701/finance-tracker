package com.uit.minhho.financetracker.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.uit.minhho.financetracker.data.repository.AppRepository;
import com.uit.minhho.financetracker.data.local.entity.Category;
import java.util.Map;
import java.util.List;

public class CategoryViewModel extends AndroidViewModel {

    private final AppRepository repository;
    private final LiveData<List<Category>> allCategories;

    public CategoryViewModel(@NonNull Application application) {
        super(application);
        repository = new AppRepository(application);
        allCategories = repository.getAllCategories();
    }

    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public LiveData<List<Category>> getCategoriesByType(boolean isIncome) {
        return repository.getCategoriesByType(isIncome);
    }

    public LiveData<Map<Integer, Double>> getCategoryUsageTotals() {
        return repository.getCategoryUsageTotals();
    }

    public void refreshCategories() {
        repository.refreshCategories();
    }

    public void insert(Category category) {
        repository.insertCategory(category);
    }

    public void insert(Category category, AppRepository.OperationCallback callback) {
        repository.insertCategory(category, callback);
    }
}
